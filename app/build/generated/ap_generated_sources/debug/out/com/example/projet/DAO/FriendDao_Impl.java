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
import com.example.projet.DTO.FriendItem;
import com.example.projet.DTO.FriendRequestItem;
import com.example.projet.DataBase.Converters;
import com.example.projet.Entities.Friend;
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
public final class FriendDao_Impl implements FriendDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Friend> __insertionAdapterOfFriend;

  private final EntityDeletionOrUpdateAdapter<Friend> __deletionAdapterOfFriend;

  private final EntityDeletionOrUpdateAdapter<Friend> __updateAdapterOfFriend;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMutual;

  private final SharedSQLiteStatement __preparedStmtOfSetFavorite;

  public FriendDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFriend = new EntityInsertionAdapter<Friend>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `friends` (`id`,`ownerUserId`,`friendUserId`,`status`,`isFavorite`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Friend entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.ownerUserId);
        statement.bindLong(3, entity.friendUserId);
        final String _tmp = Converters.fromFriendStatus(entity.status);
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
        final int _tmp_1 = entity.isFavorite ? 1 : 0;
        statement.bindLong(5, _tmp_1);
      }
    };
    this.__deletionAdapterOfFriend = new EntityDeletionOrUpdateAdapter<Friend>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `friends` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Friend entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfFriend = new EntityDeletionOrUpdateAdapter<Friend>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `friends` SET `id` = ?,`ownerUserId` = ?,`friendUserId` = ?,`status` = ?,`isFavorite` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Friend entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.ownerUserId);
        statement.bindLong(3, entity.friendUserId);
        final String _tmp = Converters.fromFriendStatus(entity.status);
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
        final int _tmp_1 = entity.isFavorite ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        statement.bindLong(6, entity.id);
      }
    };
    this.__preparedStmtOfDeleteMutual = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM friends WHERE (ownerUserId = ? AND friendUserId = ?) OR (ownerUserId = ? AND friendUserId = ?)";
        return _query;
      }
    };
    this.__preparedStmtOfSetFavorite = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE friends SET isFavorite = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insert(final Friend f) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfFriend.insertAndReturnId(f);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int delete(final Friend f) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __deletionAdapterOfFriend.handle(f);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int update(final Friend f) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __updateAdapterOfFriend.handle(f);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteMutual(final int a, final int b) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMutual.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, a);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, b);
    _argIndex = 3;
    _stmt.bindLong(_argIndex, b);
    _argIndex = 4;
    _stmt.bindLong(_argIndex, a);
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
      __preparedStmtOfDeleteMutual.release(_stmt);
    }
  }

  @Override
  public int setFavorite(final int friendRowId, final boolean fav) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfSetFavorite.acquire();
    int _argIndex = 1;
    final int _tmp = fav ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, friendRowId);
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
      __preparedStmtOfSetFavorite.release(_stmt);
    }
  }

  @Override
  public List<Friend> getIncomingRequests(final int userId) {
    final String _sql = "SELECT * FROM friends WHERE friendUserId = ? AND status = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final List<Friend> _result = new ArrayList<Friend>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Friend _item;
        _item = new Friend();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _item.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.status = Converters.toFriendStatus(_tmp);
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _item.isFavorite = _tmp_1 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Friend getAnyRelation(final int ownerId, final int friendId) {
    final String _sql = "SELECT * FROM friends WHERE ownerUserId = ? AND friendUserId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, ownerId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, friendId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final Friend _result;
      if (_cursor.moveToFirst()) {
        _result = new Friend();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _result.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.status = Converters.toFriendStatus(_tmp);
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _result.isFavorite = _tmp_1 != 0;
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
  public List<Friend> getOutgoingRequests(final int userId) {
    final String _sql = "SELECT * FROM friends WHERE ownerUserId = ? AND status = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final List<Friend> _result = new ArrayList<Friend>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Friend _item;
        _item = new Friend();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _item.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.status = Converters.toFriendStatus(_tmp);
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _item.isFavorite = _tmp_1 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Friend> getFriends(final int userId) {
    final String _sql = "SELECT * FROM friends WHERE ownerUserId = ? AND status = 'ACCEPTED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final List<Friend> _result = new ArrayList<Friend>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Friend _item;
        _item = new Friend();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _item.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.status = Converters.toFriendStatus(_tmp);
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _item.isFavorite = _tmp_1 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Friend> getForUser(final int userId) {
    final String _sql = "SELECT * FROM friends WHERE ownerUserId = ? AND status = 'ACCEPTED' ORDER BY isFavorite DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final List<Friend> _result = new ArrayList<Friend>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Friend _item;
        _item = new Friend();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _item.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.status = Converters.toFriendStatus(_tmp);
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _item.isFavorite = _tmp_1 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Friend getByOwnerAndFriend(final int ownerId, final int friendId) {
    final String _sql = "SELECT * FROM friends WHERE ownerUserId = ? AND friendUserId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, ownerId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, friendId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final Friend _result;
      if (_cursor.moveToFirst()) {
        _result = new Friend();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _result.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.status = Converters.toFriendStatus(_tmp);
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _result.isFavorite = _tmp_1 != 0;
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
  public List<FriendItem> getFriendItems(final int ownerId) {
    final String _sql = "SELECT f.id AS friendRowId, f.friendUserId AS friendUserId, u.username AS username, u.email AS email, f.isFavorite AS isFavorite, CASE WHEN ec.id IS NULL THEN 0 ELSE 1 END AS isEmergency FROM friends f INNER JOIN users u ON u.id = f.friendUserId LEFT JOIN emergency_contacts ec ON ec.ownerUserId = f.ownerUserId AND ec.friendUserId = f.friendUserId WHERE f.ownerUserId = ? AND f.status = 'ACCEPTED' ORDER BY f.isFavorite DESC, u.username ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, ownerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfFriendRowId = 0;
      final int _cursorIndexOfFriendUserId = 1;
      final int _cursorIndexOfUsername = 2;
      final int _cursorIndexOfEmail = 3;
      final int _cursorIndexOfIsFavorite = 4;
      final int _cursorIndexOfIsEmergency = 5;
      final List<FriendItem> _result = new ArrayList<FriendItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final FriendItem _item;
        _item = new FriendItem();
        _item.friendRowId = _cursor.getInt(_cursorIndexOfFriendRowId);
        _item.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _item.username = null;
        } else {
          _item.username = _cursor.getString(_cursorIndexOfUsername);
        }
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _item.email = null;
        } else {
          _item.email = _cursor.getString(_cursorIndexOfEmail);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
        _item.isFavorite = _tmp != 0;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsEmergency);
        _item.isEmergency = _tmp_1 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<FriendRequestItem> getIncomingRequestItems(final int meId) {
    final String _sql = "SELECT f.id AS friendRowId, f.ownerUserId AS requesterUserId, u.username AS username, u.email AS email FROM friends f INNER JOIN users u ON u.id = f.ownerUserId WHERE f.friendUserId = ? AND f.status = 'PENDING' ORDER BY f.id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, meId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfFriendRowId = 0;
      final int _cursorIndexOfRequesterUserId = 1;
      final int _cursorIndexOfUsername = 2;
      final int _cursorIndexOfEmail = 3;
      final List<FriendRequestItem> _result = new ArrayList<FriendRequestItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final FriendRequestItem _item;
        _item = new FriendRequestItem();
        _item.friendRowId = _cursor.getInt(_cursorIndexOfFriendRowId);
        _item.requesterUserId = _cursor.getInt(_cursorIndexOfRequesterUserId);
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _item.username = null;
        } else {
          _item.username = _cursor.getString(_cursorIndexOfUsername);
        }
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _item.email = null;
        } else {
          _item.email = _cursor.getString(_cursorIndexOfEmail);
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
  public Friend getById(final int id) {
    final String _sql = "SELECT * FROM friends WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerUserId");
      final int _cursorIndexOfFriendUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "friendUserId");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
      final Friend _result;
      if (_cursor.moveToFirst()) {
        _result = new Friend();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.ownerUserId = _cursor.getInt(_cursorIndexOfOwnerUserId);
        _result.friendUserId = _cursor.getInt(_cursorIndexOfFriendUserId);
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.status = Converters.toFriendStatus(_tmp);
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
        _result.isFavorite = _tmp_1 != 0;
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
