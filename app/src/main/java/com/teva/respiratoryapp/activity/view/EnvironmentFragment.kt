//
// EnvironmentFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view

import android.content.Context
import android.os.Bundle
import android.view.View
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.MessageShadeViewModel
import com.teva.respiratoryapp.activity.viewmodel.environment.EnvironmentViewModel
import com.teva.respiratoryapp.databinding.EnvironmentFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import com.teva.common.utilities.LocalizationService
import com.teva.environment.enumerations.AirQuality
import com.teva.environment.enumerations.PollenLevel
import com.teva.environment.enumerations.WeatherCondition

/**
 * The fragment class for the Environment screen.
 */
class EnvironmentFragment : BaseFragment<EnvironmentFragmentBinding, EnvironmentViewModel>(R.layout.environment_fragment) {
    private var messageShadeViewModel: MessageShadeViewModel? = null

    init {
        screen = AnalyticsScreen.Environment()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService?.getString(R.string.weather_text)
    }

    /**
     * Android lifecycle method called when the fragment is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()

        messageShadeViewModel?.onStart()
    }

    /**
     * Android lifecycle method called when the fragment is removed from the screen.
     */
    override fun onStop() {
        super.onStop()

        messageShadeViewModel?.onStop()
    }

    /**
     * Android lifecycle method called when the fragment is attached to an activity.

     * @param context The context for the activity.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        windSpeedUnits = dependencyProvider!!.resolve<LocalizationService>().getString(R.string.distancePerHourUnit_text)
    }

    /**
     * Sets the ViewModel for the fragment.

     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        messageShadeViewModel = MessageShadeViewModel(dependencyProvider!!)
        viewModel = EnvironmentViewModel(dependencyProvider!!)
    }

    /**
     * Called by the base class after the view is created.
     *
     * @param view The view that was created
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachScrollBehavior(view.findViewById<View>(R.id.item_list))
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.

     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: EnvironmentFragmentBinding) {
        super.initBinding(binding)

        binding.messageShade?.let { it.viewmodel = messageShadeViewModel }
    }

    companion object {

        private var windSpeedUnits: String? = null

        /**
         * This method returns the resource id of the icon to be displayed for
         * each weather condition.
         *
         * @param weatherCondition - the weather condition
         * @return - the resource id of the icon to be displayed.
         */
        @JvmStatic
        fun iconResourceForWeatherCondition(weatherCondition: WeatherCondition?): Int {
            return when(weatherCondition) {
                WeatherCondition.UNKNOWN -> R.drawable.dash_img_environment
                WeatherCondition.THUNDERSTORM -> R.drawable.ic_thunderstorm
                WeatherCondition.THUNDER -> R.drawable.ic_thunderstorm
                WeatherCondition.THUNDER2 -> R.drawable.ic_thunderstorm
                WeatherCondition.SMALL_HAIL -> R.drawable.ic_thunderstorm
                WeatherCondition.BLOWING_SAND_NEARBY -> R.drawable.ic_blowingsand
                WeatherCondition.CLEAR_NIGHT -> R.drawable.ic_clear_night
                WeatherCondition.CLOUDY -> R.drawable.ic_cloudy
                WeatherCondition.DRIZZLE -> R.drawable.ic_rain
                WeatherCondition.RAIN_SHOWER -> R.drawable.ic_rain
                WeatherCondition.FAIR_DAY -> R.drawable.ic_fairday
                WeatherCondition.FAIR_NIGHT -> R.drawable.ic_fairnight
                WeatherCondition.ICE_CRYSTALS -> R.drawable.ic_icecrystals
                WeatherCondition.MOSTLY_CLOUDY_DAY -> R.drawable.ic_partlycloudy_day
                WeatherCondition.PARTLY_CLOUDY_DAY -> R.drawable.ic_fairday
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> R.drawable.ic_partlycloudy_night
                WeatherCondition.RAIN_SNOW -> R.drawable.ic_rain_snow
                WeatherCondition.RAIN_SLEET -> R.drawable.ic_rain_snow
                WeatherCondition.SNOW_SLEET -> R.drawable.ic_rain_snow
                WeatherCondition.FREEZING_DRIZZLE -> R.drawable.ic_rain_snow
                WeatherCondition.FREEZING_RAIN -> R.drawable.ic_rain_snow
                WeatherCondition.HEAVY_SNOW_SHOWER -> R.drawable.ic_rain_snow
                WeatherCondition.SNOW_PELLETS -> R.drawable.ic_rain_snow
                WeatherCondition.RAIN -> R.drawable.ic_rain
                WeatherCondition.HEAVY_RAIN_SHOWER -> R.drawable.ic_rain
                WeatherCondition.SNOW_GRAINS -> R.drawable.ic_snow
                WeatherCondition.HEAVY_SNOW_GRAINS -> R.drawable.ic_snow
                WeatherCondition.BLOWING_SNOW -> R.drawable.ic_snow
                WeatherCondition.HEAVY_SNOW -> R.drawable.ic_snow
                WeatherCondition.WIDESPREAD_DUST -> R.drawable.ic_dust
                WeatherCondition.FOG -> R.drawable.ic_dust
                WeatherCondition.HAZE -> R.drawable.ic_dust
                WeatherCondition.SMOKE -> R.drawable.ic_dust
                WeatherCondition.SHOWERS_IN_THE_VICINITY -> R.drawable.ic_mostlycloudy_daynight
                WeatherCondition.SUNNY_DAY -> R.drawable.ic_sunny_day
                else -> R.drawable.dash_img_environment
            }
        }

        /**
         * This method returns the description to be displayed for each weather condition.
         *
         * @param weatherConditionExtendedCode - the weather condition extended code.
         * *
         * @return - the description to be displayed.
         */
        @JvmStatic
        fun descriptionForWeatherCondition(weatherConditionExtendedCode: Int?): Int {
            var weatherDescriptionResourceId = R.string.extendedWeatherCondition0_text

            if (weatherConditionExtendedCode == null) {
                return weatherDescriptionResourceId
            }

            when (weatherConditionExtendedCode) {
                400 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition400_text
                401 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition401_text
                402 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition402_text
                422 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition422_text
                429 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition429_text
                470 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition470_text
                471 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition471_text
                472 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition472_text
                480 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition480_text
                481 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition481_text
                482 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition482_text
                490 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition490_text
                491 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition491_text
                492 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition492_text
                1700 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1700_text
                1730 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1730_text
                1731 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1731_text
                1740 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1740_text
                1741 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1741_text
                1750 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1750_text
                1751 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1751_text
                1760 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1760_text
                1761 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1761_text
                1770 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1770_text
                1780 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1780_text
                1790 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1790_text
                500 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition500_text
                570 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition570_text
                580 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition580_text
                590 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition590_text
                600 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition600_text
                610 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition610_text
                640 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition640_text
                650 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition650_text
                660 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition660_text
                670 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition670_text
                680 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition680_text
                690 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition690_text
                700 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition700_text
                701 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition701_text
                705 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition705_text
                706 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition706_text
                710 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition710_text
                711 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition711_text
                715 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition715_text
                716 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition716_text
                720 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition720_text
                725 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition725_text
                730 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition730_text
                735 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition735_text
                750 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition750_text
                751 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition751_text
                755 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition755_text
                756 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition756_text
                760 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition760_text
                761 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition761_text
                765 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition765_text
                766 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition766_text
                770 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition770_text
                775 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition775_text
                780 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition780_text
                785 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition785_text
                800 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition800_text
                801 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition801_text
                802 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition802_text
                810 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition810_text
                815 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition815_text
                820 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition820_text
                825 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition825_text
                870 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition870_text
                871 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition871_text
                872 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition872_text
                880 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition880_text
                881 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition881_text
                882 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition882_text
                890 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition890_text
                891 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition891_text
                892 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition892_text
                900 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition900_text
                901 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition901_text
                902 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition902_text
                910 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition910_text
                980 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition980_text
                990 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition990_text
                991 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition991_text
                992 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition992_text
                1000 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1000_text
                1001 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1001_text
                1002 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1002_text
                1010 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1010_text
                1040 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1040_text
                1050 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1050_text
                1060 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1060_text
                1070 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1070_text
                1071 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1071_text
                1072 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1072_text
                1080 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1080_text
                1081 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1081_text
                1082 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1082_text
                1090 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1090_text
                1091 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1091_text
                1092 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1092_text
                1210 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1210_text
                1211 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1211_text
                1212 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1212_text
                1215 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1215_text
                1216 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1216_text
                1217 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1217_text
                1220 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1220_text
                1221 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1221_text
                1222 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1222_text
                1225 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1225_text
                1226 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1226_text
                1227 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1227_text
                1230 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1230_text
                1231 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1231_text
                1232 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1232_text
                1235 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1235_text
                1236 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1236_text
                1237 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1237_text
                1240 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1240_text
                1241 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1241_text
                1242 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1242_text
                1245 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1245_text
                1246 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1246_text
                1247 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1247_text
                1100 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1100_text
                1101 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1101_text
                1170 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1170_text
                1171 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1171_text
                1180 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1180_text
                1181 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1181_text
                1190 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1190_text
                1191 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1191_text
                1201 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1201_text
                1271 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1271_text
                1281 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1281_text
                1291 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1291_text
                1200 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1200_text
                1270 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1270_text
                1280 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1280_text
                1290 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1290_text
                1310 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1310_text
                1311 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1311_text
                1370 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1370_text
                1371 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1371_text
                1380 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1380_text
                1381 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1381_text
                1390 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1390_text
                1391 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1391_text
                1401 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1401_text
                1471 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1471_text
                1481 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1481_text
                1491 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1491_text
                1312 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1312_text
                1372 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1372_text
                1382 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1382_text
                1392 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1392_text
                1400 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1400_text
                1470 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1470_text
                1480 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1480_text
                1490 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1490_text
                1601 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1601_text
                1631 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1631_text
                1641 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1641_text
                1651 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1651_text
                1661 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1661_text
                1671 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1671_text
                1681 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1681_text
                1691 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1691_text
                1500 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1500_text
                1501 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1501_text
                1509 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1509_text
                1570 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1570_text
                1571 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1571_text
                1579 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1579_text
                1580 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1580_text
                1581 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1581_text
                1589 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1589_text
                1590 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1590_text
                1591 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1591_text
                1599 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1599_text
                1402 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1402_text
                1472 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1472_text
                1482 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1482_text
                1492 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1492_text
                1600 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1600_text
                1630 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1630_text
                1640 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1640_text
                1650 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1650_text
                1660 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1660_text
                1670 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1670_text
                1680 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1680_text
                1690 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1690_text
                1701 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1701_text
                1771 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1771_text
                1781 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1781_text
                1791 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1791_text
                1800 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1800_text
                1801 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1801_text
                1802 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1802_text
                1810 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1810_text
                1811 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1811_text
                1816 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1816_text
                1821 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1821_text
                1826 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1826_text
                1830 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1830_text
                1831 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1831_text
                1832 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1832_text
                1840 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1840_text
                1841 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1841_text
                1842 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1842_text
                1850 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1850_text
                1851 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1851_text
                1852 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1852_text
                1860 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1860_text
                1861 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1861_text
                1870 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1870_text
                1880 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1880_text
                1881 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1881_text
                1882 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1882_text
                1885 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1885_text
                1886 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1886_text
                1887 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1887_text
                1890 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1890_text
                1891 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1891_text
                1892 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1892_text
                1895 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1895_text
                1896 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1896_text
                1897 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1897_text
                1900 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1900_text
                1901 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1901_text
                1902 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1902_text
                1910 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1910_text
                1911 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1911_text
                1912 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1912_text
                1920 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1920_text
                1922 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1922_text
                1929 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1929_text
                1930 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1930_text
                1931 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1931_text
                1932 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1932_text
                1939 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1939_text
                1960 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1960_text
                1961 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1961_text
                1962 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1962_text
                1969 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1969_text
                1970 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1970_text
                1972 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1972_text
                1979 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1979_text
                1980 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1980_text
                1981 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1981_text
                1982 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1982_text
                1990 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1990_text
                1991 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1991_text
                1992 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1992_text
                2000 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2000_text
                2001 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2001_text
                2002 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2002_text
                2021 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2021_text
                2022 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2022_text
                2081 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2081_text
                2082 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2082_text
                2090 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2090_text
                2091 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2091_text
                2092 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2092_text
                2100 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2100_text
                2190 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2190_text
                2200 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2200_text
                2290 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2290_text
                1919 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1919_text
                1989 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1989_text
                2410 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2410_text
                2420 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2420_text
                2429 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2429_text
                2430 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2430_text
                2450 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2450_text
                2460 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2460_text
                2470 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2470_text
                2480 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2480_text
                2489 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2489_text
                2490 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2490_text
                2510 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2510_text
                2570 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2570_text
                2580 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2580_text
                2590 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2590_text
                2600 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2600_text
                2680 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2680_text
                2681 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2681_text
                2685 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2685_text
                2686 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2686_text
                2690 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2690_text
                4400 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition4400_text
                4470 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition4470_text
                4480 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition4480_text
                4490 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition4490_text
                2700 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2700_text
                2780 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2780_text
                2781 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2781_text
                2785 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2785_text
                2786 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2786_text
                2790 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2790_text
                2800 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2800_text
                2880 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2880_text
                2881 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2881_text
                2885 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2885_text
                2886 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2886_text
                2890 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2890_text
                2900 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2900_text
                2980 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2980_text
                2981 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2981_text
                2985 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2985_text
                2986 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2986_text
                2990 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition2990_text
                3000 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3000_text
                3080 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3080_text
                3081 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3081_text
                3085 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3085_text
                3086 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3086_text
                3090 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3090_text
                3100 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3100_text
                3180 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3180_text
                3181 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3181_text
                3185 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3185_text
                3186 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3186_text
                3190 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3190_text
                3200 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3200_text
                3280 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3280_text
                3281 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3281_text
                3285 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3285_text
                3286 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3286_text
                3290 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3290_text
                3300 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3300_text
                3380 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3380_text
                3381 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3381_text
                3385 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3385_text
                3386 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3386_text
                3390 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3390_text
                3400 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3400_text
                3480 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3480_text
                3481 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3481_text
                3485 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3485_text
                3486 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3486_text
                3490 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3490_text
                3840 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3840_text
                3849 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3849_text
                3870 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3870_text
                3879 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3879_text
                3880 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3880_text
                3889 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3889_text
                3890 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3890_text
                3899 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3899_text
                1102 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1102_text
                1172 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1172_text
                1182 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1182_text
                1192 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1192_text
                1202 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1202_text
                1272 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1272_text
                1282 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1282_text
                1292 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1292_text
                1602 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1602_text
                1632 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1632_text
                1642 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1642_text
                1652 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1652_text
                1662 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1662_text
                1672 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1672_text
                1682 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1682_text
                1692 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition1692_text
                3540 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3540_text
                3549 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3549_text
                3570 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3570_text
                3579 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3579_text
                3580 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3580_text
                3589 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3589_text
                3590 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3590_text
                3599 -> weatherDescriptionResourceId = R.string.extendedWeatherCondition3599_text
            }

            return weatherDescriptionResourceId
        }

        /**
         * This method returns the description to be displayed for air quality.
         *
         * @param airQuality - the air quality.
         * @return - the description to be displayed.
         */
        @JvmStatic
        fun descriptionForAirQuality(airQuality: AirQuality?): Int {
            val airQualityDescriptionResourceId = when (airQuality) {
                AirQuality.GOOD -> R.string.airQualityGood_text
                AirQuality.MODERATE -> R.string.airQualityModerate_text
                AirQuality.UNHEALTHY_FOR_SENSITIVE_GROUPS -> R.string.airQualityPoor_text
                AirQuality.UNHEALTHY -> R.string.airQualityUnhealthy_text
                AirQuality.VERY_UNHEALTHY -> R.string.airQualityVeryUnhealthy_text
                AirQuality.HAZARDOUS -> R.string.airQualityHazardous_text
                AirQuality.UNKNOWN -> R.string.environment_air_quality_unknown_text
                else -> R.string.airQualityUnknown_text
            }

            return airQualityDescriptionResourceId
        }

        /**
         * This method returns the icon to be displayed for air quality.
         *
         * @param airQuality - the air quality.
         * @return - the icon to be displayed.
         */
        @JvmStatic
        fun iconForAirQuality(airQuality: AirQuality?): Int {
            val airQualityIconResourceId = when (airQuality) {
                AirQuality.GOOD -> R.drawable.ic_airquality_good
                AirQuality.MODERATE -> R.drawable.ic_airquality_moderate
                AirQuality.UNHEALTHY_FOR_SENSITIVE_GROUPS -> R.drawable.ic_airquality_unhealthy_for_sensitive
                AirQuality.UNHEALTHY -> R.drawable.ic_airquality_unhealthy
                AirQuality.VERY_UNHEALTHY -> R.drawable.ic_airquality_very_unhealthy
                AirQuality.HAZARDOUS -> R.drawable.ic_airquality_hazardous
                else -> R.drawable.ic_airquality_unavailable
            }

            return airQualityIconResourceId
        }

        /**
         * This method returns the description to be displayed for pollen level.
         *
         * @param pollenLevel - the pollen level.
         * @return - the description to be displayed.
         */
        @JvmStatic
        fun descriptionForPollenLevel(pollenLevel: PollenLevel?): Int {
            val pollenLevelDescriptionResourceId = when (pollenLevel) {
                PollenLevel.NONE -> R.string.pollenNone_text
                PollenLevel.MODERATE -> R.string.pollenModerate_text
                PollenLevel.LOW -> R.string.pollenLow_text
                PollenLevel.HIGH -> R.string.pollenHigh_text
                PollenLevel.VERY_HIGH -> R.string.pollenVeryHigh_text
                else -> R.string.pollenNoData_text
            }

            return pollenLevelDescriptionResourceId
        }

        /**
         * This method returns the resource id of the icon to be displayed for each pollen level.
         *
         * @param pollenLevel - the pollen level.
         * @return - the resource id of the icon to be displayed.
         */
        @JvmStatic
        fun iconForPollenLevel(pollenLevel: PollenLevel?): Int {
            val pollenLevelIconResourceId = when (pollenLevel) {
                PollenLevel.NONE -> R.drawable.ic_pollen_none
                PollenLevel.MODERATE -> R.drawable.ic_pollen_moderate
                PollenLevel.LOW -> R.drawable.ic_pollen_low
                PollenLevel.HIGH -> R.drawable.ic_pollen_high
                PollenLevel.VERY_HIGH -> R.drawable.ic_pollen_very_high
                PollenLevel.NO_DATA -> R.drawable.ic_pollen_no_data
                else -> R.drawable.ic_pollen_no_data
            }

            return pollenLevelIconResourceId
        }
    }
}
