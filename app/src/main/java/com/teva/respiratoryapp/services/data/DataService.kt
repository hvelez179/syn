package com.teva.respiratoryapp.services.data

/**
 * This protocol specifies methods to create, retrieve, update, delete managed data.
 */
interface DataService {

    /**
     * Creates a new managed object from the managed object context.
     */
    fun <T : EntityProtocol> create(cls: Class<T>): T

    fun <T : EntityProtocol> insert(cls: Class<T>, objects: List<T>)

    /**
     * Deletes managed object that matches the given query information from the managed object context.
     * - Parameters:
     * - entityName: The name of the entity where the managed objects matching the query information are to be deleted from.
     * - queryInfo: Contains the search criteria for the objects to be deleted.
     */
    fun <T : EntityProtocol> delete(cls: Class<T>, queryInfo: QueryInfo)

    /**
     * Fetches a list of managed objects based on the given query information.
     * - Parameters:
     * - queryInfo: Contains the search criteria for the objects to be retrieved.
     */
    fun <T : EntityProtocol> fetchRequest(cls: Class<T>, queryInfo: QueryInfo?): List<T>

    /**
     * Gets the number of managed objects that match the search criteria.
     * - Parameters:
     * - searchCriteria: The search criteria to match the managed objects.
     */
    fun <T : EntityProtocol> getCount(cls: Class<T>, searchCriteria: SearchCriteria?): Int

    /**
     * Equivalent of calling insertOrUpdate() on an entity.  Saves the objects that match the search criteria passed in.
     * - Parameters:
     * - objects: The array of objects to save.
     * - searchCriteria: The search criteria to use against the managed objects passed in.
     */
    fun <MO : EntityProtocol> save(cls: Class<MO>, objects: List<MO>, searchCriteria: List<SearchCriteria>)

    /**
     * This method closes the data service.
     */
    fun close()

    /**
     * Indicates whether the initial data needs to be reloaded.
     */
    val shouldReloadInitialData: Boolean
}
