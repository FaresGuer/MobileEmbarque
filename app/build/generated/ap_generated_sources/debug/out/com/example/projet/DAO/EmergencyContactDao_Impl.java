package com.example.projet.DAO;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.projet.Entities.EmergencyContact;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EmergencyContactDao_Impl implements EmergencyContactDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EmergencyContact> __insertionAdapterOfEmergencyContact;

  private final EntityDeletionOrUpdateAdapter<EmergencyContact> __deletionAdapterOfEmergencyContact;

  private final EntityDeletionOrUpdateAdapter<EmergencyContact> __updateAdapterOfEmergencyContact;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByOwnerAndFriend;

  private final SharedSQLiteStatement __preparedStmtOfClearPrimaryForUser;

  public EmergencyContactDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmergencyContact = new EntityInsertionAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `emergency_contacts` (`id`,`ownerUserId`,`friendUserId`,`displayName`,`phoneNumber`,`isPrimary`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EmergencyContact entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.ownerUserId);
        if (entity.friendUserId == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.friendUserId);
        }
        if (entity.displayName == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.displayName);
        }
        if (entity.phoneNumber == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.phoneNumber);
        }
        final int _tmp = entity.isPrimary ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfEmergencyContact = new EntityDeletionOrUpdateAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `emergency_contacts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EmergencyContact entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfEmergencyContact = new EntityDeletionOrUpdateAdapter<EmergencyContact>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `emergency_contacts` SET `id` = ?,`ownerUserId` = ?,`friendUserId` = ?,`displayName` = ?,`phoneNumber` = ?,`isPrimary` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EmergencyContact entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.ownerUserId);
        if (entity.friendUserId == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.friendUserId);
        }
        if (entity.displayName == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.displayName);
        }
        if (entity.phoneNumber == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.phoneNumber);
        }
        final int _tmp = entity.isPrimary ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.id);
      }
    };
    this.__preparedStmtOfDeleteByOwnerAndFriend = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM emergency_contacts WHERE ownerUserId = ? AND friendUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearPrimaryForUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE emergency_contacts SET isPrimary = 0 WHERE ownerUserId = ?";
        return _query;
      }
    };
  }

  @Override
  public long insert(final EmergencyContact contact) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfEmergencyContact.insertAndReturnId(contact);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int delete(final EmergencyContact contact) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __deletionAdapterOfEmergencyContact.handle(contact);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int update(final EmergencyContact contact) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __updateAdapterOfEmergencyContact.handle(contact);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteByOwnerAndFriend(final int ownerId, final int friendUserId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByOwnerAndFriend.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, ownerId);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, friendUserId);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByOwnerAndFriend.release(_stmt);
    }
  }

  @Override
  public int clearPrimaryForUser(final int userId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearPrimaryForUser.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, userId);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearPrimaryForUser.release(_stmt);
    }
  }

  @Override
  public EmergencyContact getByOwnerAndFriend(final int ownerId, final int friendUserId) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE ownerUserId = ? AND friendUserId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, ownerId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, friendUserId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
      final EmergencyContact _result;
      if (_cursor.moveToFirst()) {
        _result = new EmergencyContact();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        if (_cursor.isNull(_cursorIndexOfFriendUserId)) {
          _result.friendUserId = null;
        } else {
          _result.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        }
        if (_cursor.isNull(_cursorIndexOfDisplayName)) {
          _result.displayName = null;
        } else {
          _result.displayName = _cursor.getString(_cursorIndexOfDisplayName);
        }
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _result.phoneNumber = null;
        } else {
          _result.phoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
        _result.isPrimary = _tmp != 0;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<EmergencyContact> getForUser(final int userId) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE ownerUserId = ? ORDER BY isPrimary DESC, displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
      final List<EmergencyContact> _result = new ArrayList<EmergencyContact>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EmergencyContact _item;
        _item = new EmergencyContact();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        if (_cursor.isNull(_cursorIndexOfFriendUserId)) {
          _item.friendUserId = null;
        } else {
          _item.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        }
        if (_cursor.isNull(_cursorIndexOfDisplayName)) {
          _item.displayName = null;
        } else {
          _item.displayName = _cursor.getString(_cursorIndexOfDisplayName);
        }
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _item.phoneNumber = null;
        } else {
          _item.phoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
        _item.isPrimary = _tmp != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public EmergencyContact getPrimaryForUser(final int userId) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE ownerUserId = ? AND isPrimary = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
      final EmergencyContact _result;
      if (_cursor.moveToFirst()) {
        _result = new EmergencyContact();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        if (_cursor.isNull(_cursorIndexOfFriendUserId)) {
          _result.friendUserId = null;
        } else {
          _result.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        }
        if (_cursor.isNull(_cursorIndexOfDisplayName)) {
          _result.displayName = null;
        } else {
          _result.displayName = _cursor.getString(_cursorIndexOfDisplayName);
        }
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _result.phoneNumber = null;
        } else {
          _result.phoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
        _result.isPrimary = _tmp != 0;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public EmergencyContact getById(final int id) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
      final EmergencyContact _result;
      if (_cursor.moveToFirst()) {
        _result = new EmergencyContact();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        if (_cursor.isNull(_cursorIndexOfFriendUserId)) {
          _result.friendUserId = null;
        } else {
          _result.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        }
        if (_cursor.isNull(_cursorIndexOfDisplayName)) {
          _result.displayName = null;
        } else {
          _result.displayName = _cursor.getString(_cursorIndexOfDisplayName);
        }
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _result.phoneNumber = null;
        } else {
          _result.phoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
        _result.isPrimary = _tmp != 0;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
