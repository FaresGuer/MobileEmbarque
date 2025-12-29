package com.example.projet.DataBase;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.projet.DAO.EmergencyContactDao;
import com.example.projet.DAO.EmergencyContactDao_Impl;
import com.example.projet.DAO.FriendDao;
import com.example.projet.DAO.FriendDao_Impl;
import com.example.projet.DAO.HeartRateLogDao;
import com.example.projet.DAO.HeartRateLogDao_Impl;
import com.example.projet.DAO.UserDao;
import com.example.projet.DAO.UserDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UserDao _userDao;

  private volatile EmergencyContactDao _emergencyContactDao;

  private volatile FriendDao _friendDao;

  private volatile HeartRateLogDao _heartRateLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `email` TEXT, `phoneNumber` TEXT, `dateOfBirth` TEXT, `avatarPath` TEXT, `password` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `emergency_contacts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerUserId` INTEGER NOT NULL, `friendUserId` INTEGER, `displayName` TEXT, `phoneNumber` TEXT, `isPrimary` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emergency_contacts_ownerUserId` ON `emergency_contacts` (`ownerUserId`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_emergency_contacts_ownerUserId_friendUserId` ON `emergency_contacts` (`ownerUserId`, `friendUserId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `friends` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerUserId` INTEGER NOT NULL, `friendUserId` INTEGER NOT NULL, `status` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_friends_ownerUserId` ON `friends` (`ownerUserId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_friends_friendUserId` ON `friends` (`friendUserId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `heart_rate_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerUserId` INTEGER NOT NULL, `timestampMs` INTEGER NOT NULL, `bpm` INTEGER NOT NULL, `quality` REAL NOT NULL, `isAbnormal` INTEGER NOT NULL, `isVirtual` INTEGER NOT NULL, `contextTag` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_heart_rate_logs_ownerUserId` ON `heart_rate_logs` (`ownerUserId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_heart_rate_logs_timestampMs` ON `heart_rate_logs` (`timestampMs`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_heart_rate_logs_ownerUserId_timestampMs` ON `heart_rate_logs` (`ownerUserId`, `timestampMs`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c0e188d0e31e8295d8f3a65eac98c9ac')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `emergency_contacts`");
        db.execSQL("DROP TABLE IF EXISTS `friends`");
        db.execSQL("DROP TABLE IF EXISTS `heart_rate_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(7);
        _columnsUsers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("username", new TableInfo.Column("username", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("dateOfBirth", new TableInfo.Column("dateOfBirth", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("avatarPath", new TableInfo.Column("avatarPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("password", new TableInfo.Column("password", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.example.projet.Entities.User).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsEmergencyContacts = new HashMap<String, TableInfo.Column>(6);
        _columnsEmergencyContacts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("ownerUserId", new TableInfo.Column("ownerUserId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("friendUserId", new TableInfo.Column("friendUserId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("displayName", new TableInfo.Column("displayName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("isPrimary", new TableInfo.Column("isPrimary", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmergencyContacts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEmergencyContacts = new HashSet<TableInfo.Index>(2);
        _indicesEmergencyContacts.add(new TableInfo.Index("index_emergency_contacts_ownerUserId", false, Arrays.asList("ownerUserId"), Arrays.asList("ASC")));
        _indicesEmergencyContacts.add(new TableInfo.Index("index_emergency_contacts_ownerUserId_friendUserId", true, Arrays.asList("ownerUserId", "friendUserId"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoEmergencyContacts = new TableInfo("emergency_contacts", _columnsEmergencyContacts, _foreignKeysEmergencyContacts, _indicesEmergencyContacts);
        final TableInfo _existingEmergencyContacts = TableInfo.read(db, "emergency_contacts");
        if (!_infoEmergencyContacts.equals(_existingEmergencyContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "emergency_contacts(com.example.projet.Entities.EmergencyContact).\n"
                  + " Expected:\n" + _infoEmergencyContacts + "\n"
                  + " Found:\n" + _existingEmergencyContacts);
        }
        final HashMap<String, TableInfo.Column> _columnsFriends = new HashMap<String, TableInfo.Column>(5);
        _columnsFriends.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFriends.put("ownerUserId", new TableInfo.Column("ownerUserId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFriends.put("friendUserId", new TableInfo.Column("friendUserId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFriends.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFriends.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFriends = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFriends = new HashSet<TableInfo.Index>(2);
        _indicesFriends.add(new TableInfo.Index("index_friends_ownerUserId", false, Arrays.asList("ownerUserId"), Arrays.asList("ASC")));
        _indicesFriends.add(new TableInfo.Index("index_friends_friendUserId", false, Arrays.asList("friendUserId"), Arrays.asList("ASC")));
        final TableInfo _infoFriends = new TableInfo("friends", _columnsFriends, _foreignKeysFriends, _indicesFriends);
        final TableInfo _existingFriends = TableInfo.read(db, "friends");
        if (!_infoFriends.equals(_existingFriends)) {
          return new RoomOpenHelper.ValidationResult(false, "friends(com.example.projet.Entities.Friend).\n"
                  + " Expected:\n" + _infoFriends + "\n"
                  + " Found:\n" + _existingFriends);
        }
        final HashMap<String, TableInfo.Column> _columnsHeartRateLogs = new HashMap<String, TableInfo.Column>(8);
        _columnsHeartRateLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHeartRateLogs.put("ownerUserId", new TableInfo.Column("ownerUserId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHeartRateLogs.put("timestampMs", new TableInfo.Column("timestampMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHeartRateLogs.put("bpm", new TableInfo.Column("bpm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHeartRateLogs.put("quality", new TableInfo.Column("quality", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHeartRateLogs.put("isAbnormal", new TableInfo.Column("isAbnormal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHeartRateLogs.put("isVirtual", new TableInfo.Column("isVirtual", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHeartRateLogs.put("contextTag", new TableInfo.Column("contextTag", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHeartRateLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHeartRateLogs = new HashSet<TableInfo.Index>(3);
        _indicesHeartRateLogs.add(new TableInfo.Index("index_heart_rate_logs_ownerUserId", false, Arrays.asList("ownerUserId"), Arrays.asList("ASC")));
        _indicesHeartRateLogs.add(new TableInfo.Index("index_heart_rate_logs_timestampMs", false, Arrays.asList("timestampMs"), Arrays.asList("ASC")));
        _indicesHeartRateLogs.add(new TableInfo.Index("index_heart_rate_logs_ownerUserId_timestampMs", false, Arrays.asList("ownerUserId", "timestampMs"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoHeartRateLogs = new TableInfo("heart_rate_logs", _columnsHeartRateLogs, _foreignKeysHeartRateLogs, _indicesHeartRateLogs);
        final TableInfo _existingHeartRateLogs = TableInfo.read(db, "heart_rate_logs");
        if (!_infoHeartRateLogs.equals(_existingHeartRateLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "heart_rate_logs(com.example.projet.Entities.HealthModule.HeartRateLog).\n"
                  + " Expected:\n" + _infoHeartRateLogs + "\n"
                  + " Found:\n" + _existingHeartRateLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c0e188d0e31e8295d8f3a65eac98c9ac", "42b2fe029cc313e90f01de909c691665");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","emergency_contacts","friends","heart_rate_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `emergency_contacts`");
      _db.execSQL("DELETE FROM `friends`");
      _db.execSQL("DELETE FROM `heart_rate_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EmergencyContactDao.class, EmergencyContactDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FriendDao.class, FriendDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HeartRateLogDao.class, HeartRateLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public EmergencyContactDao emergencyContactDao() {
    if (_emergencyContactDao != null) {
      return _emergencyContactDao;
    } else {
      synchronized(this) {
        if(_emergencyContactDao == null) {
          _emergencyContactDao = new EmergencyContactDao_Impl(this);
        }
        return _emergencyContactDao;
      }
    }
  }

  @Override
  public FriendDao friendDao() {
    if (_friendDao != null) {
      return _friendDao;
    } else {
      synchronized(this) {
        if(_friendDao == null) {
          _friendDao = new FriendDao_Impl(this);
        }
        return _friendDao;
      }
    }
  }

  @Override
  public HeartRateLogDao heartRateLogDao() {
    if (_heartRateLogDao != null) {
      return _heartRateLogDao;
    } else {
      synchronized(this) {
        if(_heartRateLogDao == null) {
          _heartRateLogDao = new HeartRateLogDao_Impl(this);
        }
        return _heartRateLogDao;
      }
    }
  }
}
