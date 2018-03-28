CREATE TABLE ConsentData (Z_PK INTEGER PRIMARY KEY, hasConsented INTEGER, status VARCHAR, termsAndConditions VARCHAR, privacyNotice VARCHAR, consentStartDate TIMESTAMP, consentEndDate TIMESTAMP, addressCountry VARCHAR, patientDOB TIMESTAMP, created TIMESTAMP);
ALTER TABLE DailyUserFeelingData ADD COLUMN serverTimeOffset INTEGER;
ALTER TABLE DeviceData ADD COLUMN serverTimeOffset INTEGER;
ALTER TABLE InhaleEventData ADD COLUMN serverTimeOffset INTEGER;
ALTER TABLE PrescriptionData ADD COLUMN serverTimeOffset INTEGER;
ALTER TABLE NotificationSettingData ADD COLUMN serverTimeOffset INTEGER;
CREATE TABLE UserProfileData (Z_PK INTEGER PRIMARY KEY, profileId VARCHAR, firstName VARCHAR, lastName VARCHAR, isAccountOwner INTEGER, isActive INTEGER, isEmancipated INTEGER, dateOfBirth TIMESTAMP, created TIMESTAMP, hasChanged INTEGER, changedTime TIMESTAMP, serverTimeOffset INTEGER);
CREATE TABLE ProgramData (Z_PK INTEGER PRIMARY KEY, programName VARCHAR, profileId VARCHAR, programId VARCHAR, invitationCode VARCHAR, active INTEGER);
CREATE TABLE UserAccountData (Z_PK INTEGER PRIMARY KEY, studyHashKey VARCHAR, pseudoName VARCHAR, federationId VARCHAR, username VARCHAR, identityHubIdToken VARCHAR, identityHubAccessToken VARCHAR, identityHubRefreshToken VARCHAR, identityHubProfileUrl VARCHAR, DHPAccessToken VARCHAR, DHPRefreshToken VARCHAR, lastInhalerSyncTime TIMESTAMP, lastNonInhalerSyncTime TIMESTAMP, created TIMESTAMP);
CREATE UNIQUE INDEX ZUSERPROFILE_PROFILEID_INDEX ON UserProfileData (profileId);
CREATE UNIQUE INDEX ZUSERACCOUNT_FEDERATIONID_INDEX ON UserAccountData (federationId);
CREATE UNIQUE INDEX ZPROGRAMDATA_PROFILEID_PROGRAMNAME_INDEX ON ProgramData (programName, profileId);
UPDATE Z_METADATA set version = 2 where databaseName = 'EncryptedRespiratoryApp.sqlite';
UPDATE Z_METADATA set dateModified = (select strftime('%s', 'now')) where databaseName = 'EncryptedRespiratoryApp.sqlite';