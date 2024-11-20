package aei.lang.keai;

import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

public interface FunctionI {
    String getName();

    void init(SecPlugin api, Messenger messenger, Connection conn) throws SQLException, NoSuchAlgorithmException, InvalidKeyException;

}
