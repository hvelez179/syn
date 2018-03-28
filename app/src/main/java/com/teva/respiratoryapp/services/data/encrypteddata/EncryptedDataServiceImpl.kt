//
// EncryptedDataServiceImpl.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata

import android.content.ContentValues
import android.content.Context
import android.content.res.AssetManager
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

import com.teva.respiratoryapp.BuildConfig
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.EntityProtocol
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Stopwatch

import net.sqlcipher.Cursor
import net.sqlcipher.database.SQLiteDatabase

import java.io.File

import com.teva.utilities.utilities.Logger.Level.*
import java.io.IOException
import java.util.*
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.services.data.encrypteddata.entities.*
import java.io.BufferedReader
import java.io.InputStreamReader


/**
 * The implementation of the encrypted data service.
 *
 * This uses SQLCipher to create/open and key the database.
 * This class wraps all SQLite CRUD operations.
 */
@WorkerThread
class EncryptedDataServiceImpl @MainThread
constructor(private val dependencyProvider: DependencyProvider) : DataService {

    private var database: SQLiteDatabase? = null
    private var databasePrefix: String? = null
    private var entityNameMap: MutableMap<Class<*>, String>? = null
    private val databasePasswordKey = "DatabasePassword"
    private var _shouldReloadInitialData : Boolean = false

    override val shouldReloadInitialData: Boolean
        get() = _shouldReloadInitialData

    @MainThread
    fun initializeDatabase(databasePrefix: String) {
        this.databasePrefix = databasePrefix

        entityNameMap = HashMap<Class<*>, String>()
        entityNameMap!!.put(DatabaseInfoEncrypted::class.java, "Z_METADATA")
        entityNameMap!!.put(PrescriptionDataEncrypted::class.java, "PrescriptionData")
        entityNameMap!!.put(MedicationDataEncrypted::class.java, "MedicationData")
        entityNameMap!!.put(DeviceDataEncrypted::class.java, "DeviceData")
        entityNameMap!!.put(InhaleEventDataEncrypted::class.java, "InhaleEventData")
        entityNameMap!!.put(DailyUserFeelingDataEncrypted::class.java, "DailyUserFeelingData")
        entityNameMap!!.put(ConnectionMetaDataEncrypted::class.java, "ConnectionMetaData")
        entityNameMap!!.put(NotificationSettingDataEncrypted::class.java, "NotificationSettingData")
        entityNameMap!!.put(ConsentDataEncrypted::class.java, "ConsentData")
        entityNameMap!!.put(UserProfileDataEncrypted::class.java, "UserProfileData")
        entityNameMap!!.put(ProgramDataEncrypted::class.java, "ProgramData")
        entityNameMap!!.put(UserAccountEncrypted::class.java, "UserAccountData")
        initSQLCipherDatabase()
    }

    fun deleteDatabase(databasePrefix: String) {
        this.databasePrefix = databasePrefix

        val databaseFile = databasePath
        if (databaseFile.exists()) {
            databaseFile.delete()
        }
    }

    private val databasePath: File
        get() {
            SQLiteDatabase.loadLibs(dependencyProvider.resolve<Context>())

            val context = dependencyProvider.resolve<Context>()

            return context.getDatabasePath(databasePrefix!! + ENCRYPTED_DATABASE_NAME)
        }

    private //ToDo - notify the application to prompt user to enter a passphrase
            // encrypt the passphrase provided.
            //password = get passphrase from user
            // for testing
            //ToDo - key in the key store is missing or invalid. Prompt user for passphrase,
            // re-generate the key and re-encrypt the passphrase
            // TODO - this must be generated for each installation and stored in keychain.
    val databasePassword: String
        get() {
            val applicationSettings = dependencyProvider.resolve<ApplicationSettings>()
            var password: String
            var encryptedPassword = applicationSettings.encryptedDatabasePassword

            if(encryptedPassword.isNullOrEmpty()) {
                password = generateRandomDatabasePassword()
                encryptedPassword = EncryptionHandler(dependencyProvider).encrypt(password)
                applicationSettings.encryptedDatabasePassword = encryptedPassword
            } else {
                password = EncryptionHandler(dependencyProvider).decrypt(encryptedPassword)
            }

            return password
        }

    /**
     * This function generates a random database password and returns it.
     */
    private fun generateRandomDatabasePassword(): String {
        val PASSWORD_LENGTH = 10
        val passwordBuffer = CharArray(PASSWORD_LENGTH)

        // Build a string containing all the alphanumeric characters
        // to be used in the password
        val passwordCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val random = Random()

        //randomly pick characters to form the password
        for( loop in 0 .. PASSWORD_LENGTH-1) {
            passwordBuffer[loop] = passwordCharacters[random.nextInt(passwordCharacters.length)]
        }

        return passwordBuffer.joinToString(separator = "")
    }

    /**
     * Initializes the Encrypted data store. Creates one if it doesn't exist.
     */
    @MainThread
    private fun initSQLCipherDatabase() {
        val databaseFile = databasePath

        // TODO - do proper checking for existing database, need for version upgrade, and error handling
        val createNew = !databaseFile.exists()

        if(createNew) {
            databaseFile.parentFile.mkdirs()
        }

        database = SQLiteDatabase.openOrCreateDatabase(databaseFile, databasePassword, null)

        if (createNew) {
            executeScript("createdatabaseschema.sql")
            _shouldReloadInitialData = true
        }

        updateDatabaseSchema()
    }

    /**
     * This method returns the list of schema update scripts to be run for upgrading
     * the database from its current version to the desired version.
     *
     * @param currentVersion - the current version of the database.
     * @param desiredVersion - the version to upgrade to.
     * @return - the list of scripts to be executed
     */
    private fun getUpdateScriptNames(currentVersion: Int, desiredVersion: Int): List<String> {
        val fileNames = ArrayList<String>()
        (currentVersion + 1 .. desiredVersion).mapTo(fileNames) { "schemaUpdate_$it.sql" }
        return fileNames
    }

    /**
     * This method executes the specified script file from the App assets.
     *
     * @param scriptFileName - the name of the script file to execute.
     */
    private fun executeScript(scriptFileName: String) {
        logger.log(INFO, "Executing script $scriptFileName")
        val assetManager = dependencyProvider.resolve<AssetManager>()
        try {
            val inputStream = assetManager.open(scriptFileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line = reader.readLine()

            while (line != null) {
                database!!.execSQL(line)
                line = reader.readLine()
            }
        } catch(ex: IOException) {
            logger.logException(ERROR, "Failed to open $scriptFileName", ex)
            throw ex
        }
        logger.log(INFO, "Executed script $scriptFileName")
    }

    /**
     * This method updates the database schema.
     */
    private fun updateDatabaseSchema() {
        val databaseSchemaVersion = Integer.parseInt(schemaVersion)
        val desiredSchemaVersion = BuildConfig.SCHEMA_VERSION

        if(databaseSchemaVersion < desiredSchemaVersion) {
            val updateScriptNames = getUpdateScriptNames(databaseSchemaVersion, desiredSchemaVersion)

            for(scriptName in updateScriptNames) {
                executeScript(scriptName)
            }

            _shouldReloadInitialData = true
        }
    }

    /**
     * Creates a new managed object from the managed object context.
     * @param cls The class of the object to create.
     */
    override fun <MO : EntityProtocol> create(cls: Class<MO>): MO {
        val result: MO
        try {
            result = cls.getConstructor().newInstance() as MO
        } catch (e: Exception) {
            logger.log(ERROR, "Exception while creating " + cls.simpleName, cls)
            throw e
        }

        return result
    }

    /**
     * Deletes managed object that matches the given query information from the managed object context.
     * @param queryInfo Contains the search criteria for the objects to be deleted.
     */
    override fun <MO : EntityProtocol> delete(cls: Class<MO>, queryInfo: QueryInfo) {
        val delete = StatementWithPredicate()
        delete.searchCriteria = queryInfo.searchCriteria
        val rows = database!!.delete(entityNameMap!![cls], delete.whereClauseString, delete.whereClauseArguments)
        logger.log(DEBUG, Integer.toString(rows) + " rows deleted from database table " + entityNameMap!![cls])
    }

    /**
     * This method inserts a set of records in the database.
     * @param cls - the type of the object being inserted. This information is used to identify
     * *            the table in which data needs to be inserted.
     * *
     * @param objects - the objects containing the data to be inserted.
     */
    override fun <MO : EntityProtocol> insert(cls: Class<MO>, objects: List<MO>) {
        // SQLite by default uses a separate transaction for each insert.
        // To override this behavior and overcome the associated performance hit,
        // we create a transaction for multiple inserts.
        database!!.beginTransaction()
        try {
            for (entity in objects) {
                val rows = database!!.insert(entityNameMap!![cls], null, convertParametersToContentValues((entity as EncryptedEntity).schemaMap))
                logger.log(DEBUG, java.lang.Long.toString(rows) + " rows inserted in database table " + entityNameMap!![cls])
            }
            database!!.setTransactionSuccessful()
        } finally {
            database!!.endTransaction()
        }
    }

    /**
     * This method updates a set of records in the database.
     * @param cls - the type of the object being updated. This information is used to identify
     * *            the table in which data needs to be updated.
     * *
     * @param objects - the objects containing the data to be updated in the database.
     * *
     * @param searchCriteria -  the search criteria for each of the objects to be updated.
     */
    fun <MO : EntityProtocol> update(cls: Class<MO>, objects: List<MO>, searchCriteria: List<SearchCriteria>) {
        var index = 0
        database!!.beginTransaction()
        try {
            for (entity in objects) {
                val update = StatementWithPredicate()
                update.searchCriteria = searchCriteria[index]
                val updatedRows = database!!.update(entityNameMap!![cls], convertParametersToContentValues((entity as EncryptedEntity).schemaMap), update.whereClauseString, update.whereClauseArguments)
                logger.log(DEBUG, Integer.toString(updatedRows) + " rows updated in database table " + entityNameMap!![cls])
                index++
            }
            database!!.setTransactionSuccessful()
        } finally {
            database!!.endTransaction()
        }
    }


    /**
     * Fetches a list of managed objects based on the given query information.
     * @param cls The class of objects to fetch.
     * *
     * @param queryInfo Contains the search criteria for the objects to be retrieved.
     */
    override fun <T : EntityProtocol> fetchRequest(cls: Class<T>, queryInfo: QueryInfo?): List<T> {
        val fetchResultSet = ArrayList<T>()

        val fetch = EncryptedFetch()
        fetch.setTableName(entityNameMap!![cls]!!)

        fetch.setQueryInfo(queryInfo)

        val sql = fetch.queryString

        logger.log(DEBUG, "fetchRequest: " + sql)
        val stopwatch = Stopwatch.Start(logger)

        val cursor = database!!.rawQuery(sql, fetch.whereClauseArguments)
        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val entity = create(cls)
                    entity.isNew = false
                    (entity as EncryptedEntity).schemaMap = createSchemaMap(cursor)
                    fetchResultSet.add(entity)
                } while (cursor.moveToNext())
            }
        }

        stopwatch.mark(DEBUG, "fetchRequest: returned " + fetchResultSet.size + " objects")

        return fetchResultSet
    }

    /**
     * Gets the number of managed objects that match the search criteria.
     * - Parameters:
     * - searchCriteria: The search criteria to match the managed objects.

     * @param cls - the type of the objects to search.
     * *
     * @param searchCriteria -  the search criteria for searching.
     */
    override fun <T : EntityProtocol> getCount(cls: Class<T>, searchCriteria: SearchCriteria?): Int {
        val encryptedCount = EncryptedCount()
        encryptedCount.setTableName(entityNameMap!![cls]!!)

        encryptedCount.searchCriteria = searchCriteria

        val sql = encryptedCount.queryString
        logger.log(DEBUG, "countRequest: " + sql!!)
        val recordCount = database!!.rawQuery(sql, encryptedCount.whereClauseArguments)
        var count = 0
        recordCount.use { recordCount ->
            recordCount.moveToFirst()
            count = recordCount.getInt(0)
        }

        logger.log(DEBUG, "countRequest: returned " + Integer.toString(count))

        return count
    }

    private fun createSchemaMap(cursor: Cursor): MutableMap<String, Any?> {
        val schemaMap = HashMap<String, Any?>()

        val count = cursor.columnCount
        for (i in 0..count - 1) {
            val name = cursor.getColumnName(i)
            var obj: Any? = null
            when (cursor.getType(i)) {
                Cursor.FIELD_TYPE_INTEGER -> obj = cursor.getInt(i)

                Cursor.FIELD_TYPE_FLOAT -> obj = cursor.getFloat(i)

                Cursor.FIELD_TYPE_STRING -> obj = cursor.getString(i)
            }

            schemaMap.put(name, obj)
        }

        return schemaMap
    }

    /**
     * Equivalent of calling insertOrUpdate() on an entity.  Saves the objects that match the search criteria passed in.
     * @param objects The array of objects to save.
     * *
     * @param searchCriteria The search criteria to use against the managed objects passed in.
     */
    override fun <MO : EntityProtocol> save(cls: Class<MO>, objects: List<MO>, searchCriteria: List<SearchCriteria>) {
        val insertEntities = ArrayList<MO>()
        val updateEntities = ArrayList<MO>()
        val updateCriteria = ArrayList<SearchCriteria>()

        for (i in objects.indices) {
            val entity = objects[i]
            if (entity.isNew) {
                insertEntities.add(entity)
            } else {
                updateEntities.add(entity)
                updateCriteria.add(searchCriteria[i])
            }
        }

        if (insertEntities.size > 0) {
            insert(cls, insertEntities)
        }

        if (updateEntities.size > 0) {
            update(cls, updateEntities, updateCriteria)
        }
    }

    /**
     * This method closes the data service.
     */
    override fun close() {

    }

    val schemaVersion: String
        get() {
            val databaseInfo = fetchRequest(DatabaseInfoEncrypted::class.java, null)

            return databaseInfo[0].version
        }

    companion object {
        private val logger = Logger(EncryptedDataServiceImpl::class)

        private val ENCRYPTED_DATABASE_NAME = "EncryptedRespiratoryApp.sqlite"

        /**
         * This method converts a map containing column names and corresponding values
         * to be inserted or updated in the database into a  ContentValues object
         * used by the SQLiteDatabase for insert and update.
         * @param parameters - a map holding the column names and the values to be inserted or updated.
         * *
         * @return - a ContentValues object populated with the data to be inserted or updated.
         */
        internal fun convertParametersToContentValues(parameters: Map<String, Any?>): ContentValues {
            val contentValues = ContentValues()

            for (key in parameters.keys) {
                val value = parameters[key]
                if(value == null) {
                    contentValues.putNull(key)
                } else if (value is Int) {
                    contentValues.put(key, value)
                } else if (value is String) {
                    contentValues.put(key, value)
                } else if (value is Byte) {
                    contentValues.put(key, value)
                } else if (value is Short) {
                    contentValues.put(key, value)
                } else if (value is Long) {
                    contentValues.put(key, value)
                } else if (value is Float) {
                    contentValues.put(key, value)
                } else if (value is Double) {
                    contentValues.put(key, value)
                } else if (value is ByteArray) {
                    contentValues.put(key, value)
                } else if (value is Boolean) {
                    contentValues.put(key, value)
                }
            }

            return contentValues
        }
    }
}
