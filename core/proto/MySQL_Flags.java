/*
 * Just a list of important flags that the Proxy code uses.
 * No need to keep it in the same file, so I broke it out
 * and put them here
 */

interface MySQL_Flags {
    public static final int MODE_INIT                           = 0;  // Connection opened
    public static final int MODE_READ_HANDSHAKE                 = 1;  // Read the handshake from the server and process it
    public static final int MODE_SEND_HANDSHAKE                 = 2;  // Forward the handshake from the server
    public static final int MODE_READ_AUTH                      = 3;  // Read the reply from the client and process it
    public static final int MODE_SEND_AUTH                      = 4;  // Forward the reply from the client
    public static final int MODE_READ_AUTH_RESULT               = 5;  // Read the reply from the server and process it
    public static final int MODE_SEND_AUTH_RESULT               = 6;  // Forward the reply from the server
    public static final int MODE_READ_QUERY                     = 7;  // Read the query from the client and process it
    public static final int MODE_SEND_QUERY                     = 8;  // Send the query to the server
    public static final int MODE_READ_QUERY_RESULT              = 9;  // Read the result set from the server and and process it
    public static final int MODE_SEND_QUERY_RESULT              = 10; // Send a result set to the client
    public static final int MODE_CLEANUP                        = 11; // Connection closed
    
    // Packet types
    public static final byte COM_SLEEP                          = (byte)0x00; // deprecated
    public static final byte COM_QUIT                           = (byte)0x01;
    public static final byte COM_INIT_DB                        = (byte)0x02;
    public static final byte COM_QUERY                          = (byte)0x03;
    public static final byte COM_FIELD_LIST                     = (byte)0x04;
    public static final byte COM_CREATE_DB                      = (byte)0x05;
    public static final byte COM_DROP_DB                        = (byte)0x06;
    public static final byte COM_REFRESH                        = (byte)0x07;
    public static final byte COM_SHUTDOWN                       = (byte)0x08;
    public static final byte COM_STATISTICS                     = (byte)0x09;
    public static final byte COM_PROCESS_INFO                   = (byte)0x0a; // deprecated
    public static final byte COM_CONNECT                        = (byte)0x0b; // deprecated
    public static final byte COM_PROCESS_KILL                   = (byte)0x0c;
    public static final byte COM_DEBUG                          = (byte)0x0d;
    public static final byte COM_PING                           = (byte)0x0e;
    public static final byte COM_TIME                           = (byte)0x0f; // deprecated
    public static final byte COM_DELAYED_INSERT                 = (byte)0x10; // deprecated
    public static final byte COM_CHANGE_USER                    = (byte)0x11;
    public static final byte COM_BINLOG_DUMP                    = (byte)0x12;
    public static final byte COM_TABLE_DUMP                     = (byte)0x13;
    public static final byte COM_CONNECT_OUT                    = (byte)0x14;
    public static final byte COM_REGISTER_SLAVE                 = (byte)0x15;
    public static final byte COM_STMT_PREPARE                   = (byte)0x16;
    public static final byte COM_STMT_EXECUTE                   = (byte)0x17;
    public static final byte COM_STMT_SEND_LONG_DATA            = (byte)0x18;
    public static final byte COM_STMT_CLOSE                     = (byte)0x19;
    public static final byte COM_STMT_RESET                     = (byte)0x1a;
    public static final byte COM_SET_OPTION                     = (byte)0x1b;
    public static final byte COM_STMT_FETCH                     = (byte)0x1c;
    public static final byte COM_DAEMON                         = (byte)0x1d; // deprecated
    public static final byte COM_UNKNOWN                        = (byte)0xff; // bad!
    
    public static final byte OK                                 = (byte)0x00;
    public static final byte ERR                                = (byte)0xff;
    public static final byte EOF                                = (byte)0xfe;
    
    public static final int SERVER_STATUS_IN_TRANS              = 0x0001;
    public static final int SERVER_STATUS_AUTOCOMMIT            = 0x0002;
    public static final int SERVER_MORE_RESULTS_EXISTS          = 0x0008;
    public static final int SERVER_STATUS_NO_GOOD_INDEX_USED    = 0x0010;
    public static final int SERVER_STATUS_NO_INDEX_USED         = 0x0020;
    public static final int SERVER_STATUS_CURSOR_EXISTS         = 0x0040;
    public static final int SERVER_STATUS_LAST_ROW_SENT         = 0x0080;
    public static final int SERVER_STATUS_DB_DROPPED            = 0x0100;
    public static final int SERVER_STATUS_NO_BACKSLASH_ESCAPES  = 0x0200;
    public static final int SERVER_STATUS_METADATA_CHANGED      = 0x0400;
    public static final int SERVER_QUERY_WAS_SLOW               = 0x0800;
    public static final int SERVER_PS_OUT_PARAMS                = 0x1000;
    
    public static final int CLIENT_LONG_PASSWORD                = 0x0001;
    public static final int CLIENT_FOUND_ROWS                   = 0x0002;
    public static final int CLIENT_LONG_FLAG                    = 0x0004;
    public static final int CLIENT_CONNECT_WITH_DB              = 0x0008;
    public static final int CLIENT_NO_SCHEMA                    = 0x0010;
    public static final int CLIENT_COMPRESS                     = 0x0020;
    public static final int CLIENT_ODBC                         = 0x0040;
    public static final int CLIENT_LOCAL_FILES                  = 0x0080;
    public static final int CLIENT_IGNORE_SPACE                 = 0x0100;
    public static final int CLIENT_PROTOCOL_41                  = 0x0200;
    public static final int CLIENT_INTERACTIVE                  = 0x0400;
    public static final int CLIENT_SSL                          = 0x0800;
    public static final int CLIENT_IGNORE_SIGPIPE               = 0x1000;
    public static final int CLIENT_TRANSACTIONS                 = 0x2000;
    public static final int CLIENT_RESERVED                     = 0x4000;
    public static final int CLIENT_SECURE_CONNECTION            = 0x8000;
    public static final int CLIENT_MULTI_STATEMENTS             = 0x00010000;
    public static final int CLIENT_MULTI_RESULTS                = 0x00020000;
    public static final int CLIENT_PS_MULTI_RESULTS             = 0x00040000;
    public static final int CLIENT_SSL_VERIFY_SERVER_CERT       = 0x40000000;
    public static final int CLIENT_REMEMBER_OPTIONS             = 0x80000000;
    
    public static final int MYSQL_TYPE_DECIMAL                  = 0x00;
    public static final int MYSQL_TYPE_TINY                     = 0x01;
    public static final int MYSQL_TYPE_SHORT                    = 0x02;
    public static final int MYSQL_TYPE_LONG                     = 0x03;
    public static final int MYSQL_TYPE_FLOAT                    = 0x04;
    public static final int MYSQL_TYPE_DOUBLE                   = 0x05;
    public static final int MYSQL_TYPE_NULL                     = 0x06;
    public static final int MYSQL_TYPE_TIMESTAMP                = 0x07;
    public static final int MYSQL_TYPE_LONGLONG                 = 0x08;
    public static final int MYSQL_TYPE_INT24                    = 0x09;
    public static final int MYSQL_TYPE_DATE                     = 0x0a;
    public static final int MYSQL_TYPE_TIME                     = 0x0b;
    public static final int MYSQL_TYPE_DATETIME                 = 0x0c;
    public static final int MYSQL_TYPE_YEAR                     = 0x0d;
    public static final int MYSQL_TYPE_NEWDATE                  = 0x0e;
    public static final int MYSQL_TYPE_VARCHAR                  = 0x0f;
    public static final int MYSQL_TYPE_BIT                      = 0x10;
    public static final int MYSQL_TYPE_NEWDECIMAL               = 0xf6;
    public static final int MYSQL_TYPE_ENUM                     = 0xf7;
    public static final int MYSQL_TYPE_SET                      = 0xf8;
    public static final int MYSQL_TYPE_TINY_BLOB                = 0xf9;
    public static final int MYSQL_TYPE_MEDIUM_BLOB              = 0xfa;
    public static final int MYSQL_TYPE_LONG_BLOB                = 0xfb;
    public static final int MYSQL_TYPE_BLOB                     = 0xfc;
    public static final int MYSQL_TYPE_VAR_STRING               = 0xfd;
    public static final int MYSQL_TYPE_STRING                   = 0xfe;
    public static final int MYSQL_TYPE_GEOMETRY                 = 0xff;

    public static final int REFRESH_GRANT                       = 0x01; 
    public static final int REFRESH_LOG                         = 0x02; 
    public static final int REFRESH_TABLES                      = 0x04; 
    public static final int REFRESH_HOSTS                       = 0x08; 
    public static final int REFRESH_STATUS                      = 0x10; 
    public static final int REFRESH_THREADS                     = 0x20; 
    public static final int REFRESH_SLAVE                       = 0x40; 
    public static final int REFRESH_MASTER                      = 0x80;
    
    public static final int SHUTDOWN_DEFAULT                    = 0x00; 
    public static final int SHUTDOWN_WAIT_CONNECTIONS           = 0x01; 
    public static final int SHUTDOWN_WAIT_TRANSACTIONS          = 0x02; 
    public static final int SHUTDOWN_WAIT_UPDATES               = 0x08; 
    public static final int SHUTDOWN_WAIT_ALL_BUFFERS           = 0x10; 
    public static final int SHUTDOWN_WAIT_CRITICAL_BUFFERS      = 0x11; 
    public static final int KILL_QUERY                          = 0xfe; 
    public static final int KILL_CONNECTION                     = 0xff;
 
    public static final int CURSOR_TYPE_NO_CURSOR               = 0x00;
    public static final int CURSOR_TYPE_READ_ONLY               = 0x01;
    public static final int CURSOR_TYPE_FOR_UPDATE              = 0x02;
    public static final int CURSOR_TYPE_SCROLLABLE              = 0x04;
    
    public static final int MYSQL_OPTION_MULTI_STATEMENTS_ON    = 0;
    public static final int MYSQL_OPTION_MULTI_STATEMENTS_OFF   = 1;
    
    public static final int RS_OK                               = 0;
    public static final int RS_FULL                             = 1;
    public static final int RS_COL_DEF                          = 2;
    
}
