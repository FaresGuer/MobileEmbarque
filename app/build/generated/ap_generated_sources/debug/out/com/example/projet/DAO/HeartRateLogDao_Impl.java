package com.example.projet.DAO;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.projet.Entities.HealthModule.HeartRateLog;
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
public final class HeartRateLogDao_Impl implements HeartRateLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HeartRateLog> __insertionAdapterOfHeartRateLog;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllForUser;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOlderThan;

  public HeartRateLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHeartRateLog = new EntityInsertionAdapter<HeartRateLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `heart_rate_logs` (`id`,`ownerUserId`,`timestampMs`,`bpm`,`quality`,`isAbnormal`,`isVirtual`,`contextTag`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final HeartRateLog entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.ownerUserId);
        statement.bindLong(3, entity.timestampMs);
        statement.bindLong(4, entity.bpm);
        statement.bindDouble(5, entity.quality);
        final int _tmp = entity.isAbnormal ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isVirtual ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        if (entity.contextTag == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.contextTag);
        }
      }
    };
    this.__preparedStmtOfDeleteAllForUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM heart_rate_logs WHERE ownerUserId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM heart_rate_logs WHERE timestampMs < ?";
        return _query;
      }
    };
  }

  @Override
  public long insert(final HeartRateLog log) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfHeartRateLog.insertAndReturnId(log);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteAllForUser(final int userId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllForUser.acquire();
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
      __preparedStmtOfDeleteAllForUser.release(_stmt);
    }
  }

  @Override
  public int deleteOlderThan(final long olderThanMs) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOlderThan.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, olderThanMs);
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
      __preparedStmtOfDeleteOlderThan.release(_stmt);
    }
  }

  @Override
  public List<HeartRateLog> getForUser(final int userId) {
    final String _sql = "SELECT * FROM heart_rate_logs WHERE ownerUserId = ? ORDER BY timestampMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
      final int _cursorIndexOfBpm = CursorUtil.getColumnIndexOrThrow(_cursor, "bpm");
      final int _cursorIndexOfQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "quality");
      final int _cursorIndexOfIsAbnormal = CursorUtil.getColumnIndexOrThrow(_cursor, "isAbnormal");
      final int _cursorIndexOfIsVirtual = CursorUtil.getColumnIndexOrThrow(_cursor, "isVirtual");
      final int _cursorIndexOfContextTag = CursorUtil.getColumnIndexOrThrow(_cursor, "contextTag");
      final List<HeartRateLog> _result = new ArrayList<HeartRateLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final HeartRateLog _item;
        _item = new HeartRateLog();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _item.timestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
        _item.bpm = _cursor.getInt(_cursorIndexOfBpm);
        _item.quality = _cursor.getFloat(_cursorIndexOfQuality);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAbnormal);
        _item.isAbnormal = _tmp != 0;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsVirtual);
        _item.isVirtual = _tmp_1 != 0;
        if (_cursor.isNull(_cursorIndexOfContextTag)) {
          _item.contextTag = null;
        } else {
          _item.contextTag = _cursor.getString(_cursorIndexOfContextTag);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<HeartRateLog> getAbnormalForUser(final int userId) {
    final String _sql = "SELECT * FROM heart_rate_logs WHERE ownerUserId = ? AND isAbnormal = 1 ORDER BY timestampMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
      final int _cursorIndexOfBpm = CursorUtil.getColumnIndexOrThrow(_cursor, "bpm");
      final int _cursorIndexOfQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "quality");
      final int _cursorIndexOfIsAbnormal = CursorUtil.getColumnIndexOrThrow(_cursor, "isAbnormal");
      final int _cursorIndexOfIsVirtual = CursorUtil.getColumnIndexOrThrow(_cursor, "isVirtual");
      final int _cursorIndexOfContextTag = CursorUtil.getColumnIndexOrThrow(_cursor, "contextTag");
      final List<HeartRateLog> _result = new ArrayList<HeartRateLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final HeartRateLog _item;
        _item = new HeartRateLog();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _item.timestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
        _item.bpm = _cursor.getInt(_cursorIndexOfBpm);
        _item.quality = _cursor.getFloat(_cursorIndexOfQuality);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAbnormal);
        _item.isAbnormal = _tmp != 0;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsVirtual);
        _item.isVirtual = _tmp_1 != 0;
        if (_cursor.isNull(_cursorIndexOfContextTag)) {
          _item.contextTag = null;
        } else {
          _item.contextTag = _cursor.getString(_cursorIndexOfContextTag);
        }
        _result.add(_item);
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
