//
// LocationServiceTests.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.location.services

import android.content.Context
import android.location.*
import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.PermissionManager
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.location.services.utilities.LocationServiceMatchers.matchesLocationInfo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class LocationServiceTests {

    private val googleLocationClient: GoogleLocationClient = mock()
    private val context: Context = mock()
    private val dependencyProvider: DependencyProvider = DependencyProvider.default
    private val permissionManager: PermissionManager = mock()
    private val messenger: Messenger = mock()
    private val locationCallback: LocationCallback = mock()
    private val delegate: LocationServiceDelegate = mock()
    private val location: Location = mock()
    private val address: Address = mock()
    private val locationManager: LocationManager = mock()
    private val locationSettings: LocationSettings = mock()
    private val geocoder: Geocoder = mock()

    /**
     * This method sets up the mocks for classes and methods required for running the tests
     * @throws Exception - GoogleApiClient.Builder constructor needs to be mocked as the object cannot
     * *                      be instantiated in tests and mocking the constructor requires
     * *                      java.lang.Exception to be handled
     */
    @Before
    @Throws(Exception::class)
    fun setup() {
        DependencyProvider.default.unregisterAll()

        dependencyProvider.register(PermissionManager::class, permissionManager)
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(LocationManager::class, locationManager)
        dependencyProvider.register(Context::class, context)
        dependencyProvider.register(Geocoder::class, geocoder)

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager)
        whenever(permissionManager.checkPermission(any())).thenReturn(true)
        doNothing().whenever(messenger).subscribe(any())
        doNothing().whenever(locationCallback).locationLookupCompleted(any())
        whenever(locationManager.getBestProvider(any(), any())).thenReturn("Provider")
        doNothing().whenever(locationManager).requestLocationUpdates(any<String>(), any<Long>(), any<Float>(), any<LocationListener>())

        dependencyProvider.register(GoogleLocationClient::class, googleLocationClient)

        whenever(location.latitude).thenReturn(40.70387)
        whenever(location.longitude).thenReturn(-74.013854)

        whenever(address.latitude).thenReturn(40.70387)
        whenever(address.latitude).thenReturn(40.70387)
        whenever(address.longitude).thenReturn(-74.013854)
        whenever(address.getAddressLine(any())).thenReturn("New York")
        whenever(address.countryName).thenReturn("USA")
        whenever(address.locality).thenReturn("10004")
        whenever(address.adminArea).thenReturn("NY")

        whenever(locationSettings.isLocationModeOn).thenReturn(true)
        dependencyProvider.register(LocationSettings::class, locationSettings)

    }

    @Test
    fun testLatitudeAndLongitudeAreReturnedWhenCityIsProvided() {
        // initialize test data
        val addresses = ArrayList<Address>()
        addresses.add(address)


        whenever(geocoder.getFromLocationName(any(), any())).thenReturn(addresses)

        // create expectations
        val expectedLocationInfo = LocationInfo(40.70387, -74.013854, "New York", "10004", "NY", "USA")

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)
        locationService.startService()
        val locationInfoCaptor = argumentCaptor<LocationInfo>()
        locationService.locationLookup("New York", locationCallback)

        // test expectations

        // the look up runs on a separate thread so specify a timeout to allow the callback to be invoked
        verify(locationCallback, timeout(10000)).locationLookupCompleted(locationInfoCaptor.capture())
        assertThat(locationInfoCaptor.lastValue, matchesLocationInfo(expectedLocationInfo))
    }

    @Test
    fun testAddressIsReturnedFromReverseLocationLookup() {

        // initialize test data
        val addresses = ArrayList<Address>()
        addresses.add(address)

        whenever(geocoder.getFromLocation(any(), any(), any())).thenReturn(addresses)

        // create expectations
        val expectedLocationInfo = LocationInfo(40.70387, -74.013854, "New York", "10004", "NY", "USA")

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)
        locationService.startService()
        val locationInfoCaptor = argumentCaptor<LocationInfo>()
        locationService.reverseLocationLookup(40.70387, -74.013854, locationCallback)

        // test expectations

        // the look up runs on a separate thread so specify a timeout to allow the callback to be invoked
        verify(locationCallback, timeout(10000)).locationLookupCompleted(locationInfoCaptor.capture())
        assertThat(locationInfoCaptor.lastValue, matchesLocationInfo(expectedLocationInfo))
    }

    @Test
    fun testCurrentLocationUpdatedCallbackCalledOnLocationChange() {
        // create expectations
        val latitude = 40.70387
        val longitude = -74.013854

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)
        locationService.locationServiceDelegate = delegate
        locationService.startService()
        val latitudeCaptor = argumentCaptor<Double>()
        val longitudeCaptor = argumentCaptor<Double>()
        //locationServicesProvider.onLocationChanged(location);
        locationService.onLocationChanged(location)

        // text expectations
        verify(delegate).currentLocationUpdated(latitudeCaptor.capture(), longitudeCaptor.capture())
        assertEquals(latitude, latitudeCaptor.lastValue, 0.001)
        assertEquals(longitude, longitudeCaptor.lastValue, 0.001)
    }

    @Test
    fun testLocationServiceIsAvailableWhenStartedWithPermissions() {

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)
        locationService.startService()
        val available = locationService.isAvailable

        // test expectations
        assertEquals(available, true)
    }

    @Test
    fun testLocationServiceIsUnavailableWhenStartedWithoutPermissions() {

        // perform operation
        whenever(permissionManager.checkPermission(any())).thenReturn(false)
        val locationService = LocationServiceImpl(dependencyProvider)
        locationService.startService()
        val available = locationService.isAvailable

        // test expectations
        assertEquals(available, false)
    }

    @Test
    fun testLocationServiceUnavailableIfNotStarted() {

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)
        val available = locationService.isAvailable

        // test expectations
        assertEquals(available, false)
    }

    @Test
    fun testStopServiceCleansUpLocationService() {

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)

        locationService.startService()
        locationService.stopService()

        // test expectations
        verify(googleLocationClient).uninitialize()
        verify(locationManager).removeUpdates(any<LocationListener>())
    }

    @Test
    fun testStopServiceWithoutStartingCleansUpLocationService() {

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)
        locationService.stopService()

        // test expectations
        verify(locationManager, never()).removeUpdates(any<LocationListener>())
    }

    @Test
    fun testServiceReturnsNullForLocationWhenNotStarted() {

        // perform operation
        val locationService = LocationServiceImpl(dependencyProvider)
        locationService.locationLookup("New York", locationCallback)
        locationService.reverseLocationLookup(0.0, 0.0, locationCallback)
        val currentLocation = locationService.currentLocation
        locationService.stopService()

        // text expectations
        assertNull(currentLocation)
        verify(locationCallback, atLeast(2)).locationLookupCompleted(isNull())
    }
}
