package org.apache.ambari.server.controller;

import java.sql.SQLException;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.configuration.Configuration;
import org.apache.ambari.server.orm.DBAccessor;
import org.apache.ambari.server.orm.dao.MetainfoDAO;
import org.apache.ambari.server.orm.entities.MetainfoEntity;
import org.apache.ambari.server.security.authorization.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class DatabaseInitializer {
    private static Logger LOG = LoggerFactory
            .getLogger(DatabaseInitializer.class);
    @Inject
    protected DBAccessor dbAccessor;
    @Inject
    protected Configuration configuration;

    @Inject
    protected Injector injector;

    @Inject
    AmbariMetaInfo ambariMetaInfo;

    @Inject
    MetainfoDAO metainfoDAO;
    
    @Inject
    protected PasswordEncoder passwordEncoder;

    public void initializeDatabase() throws AmbariException {

        Users users = injector.getInstance(Users.class);
        String userName = "admin";
        if (users.getAnyUser(userName) != null) {
            LOG.info("Database is already initialized.");
            return;
        }
        try {
            insertInitialDBValues();
        } catch (Exception exception) {
            throw new AmbariException("Database initialization failed.",
                    exception);
        }
    }

    private void insertInitialDBValues() throws SQLException {
        
        dbAccessor.insertRow("adminresourcetype", new String[]{"resource_type_id", "resource_type_name"}, new String[]{"1", "'AMBARI'"}, false);
        dbAccessor.insertRow("adminresourcetype", new String[]{"resource_type_id", "resource_type_name"}, new String[]{"2", "'CLUSTER'"}, false);
        dbAccessor.insertRow("adminresourcetype", new String[]{"resource_type_id", "resource_type_name"}, new String[]{"3", "'VIEW'"}, false);
        dbAccessor.insertRow("adminresource", new String[] { "resource_id", "resource_type_id" }, new String[] { "1", "1" }, false);
        
        dbAccessor.insertRow("adminprincipaltype", new String[]{"principal_type_id", "principal_type_name"}, new String[]{"1", "'USER'"}, false);
        dbAccessor.insertRow("adminprincipaltype", new String[]{"principal_type_id", "principal_type_name"}, new String[]{"2", "'GROUP'"}, false);

        dbAccessor.insertRow("adminprincipal", new String[]{"principal_id", "principal_type_id"}, new String[]{"1", "1"}, false);
        
        
        dbAccessor.insertRow("adminpermission", new String[]{"permission_id", "permission_name", "resource_type_id"}, new String[]{"1", "'AMBARI.ADMIN'", "1"}, false);
        dbAccessor.insertRow("adminpermission", new String[]{"permission_id", "permission_name", "resource_type_id"}, new String[]{"2", "'CLUSTER.READ'", "2"}, false);
        dbAccessor.insertRow("adminpermission", new String[]{"permission_id", "permission_name", "resource_type_id"}, new String[]{"3", "'CLUSTER.OPERATE'", "2"}, false);
        dbAccessor.insertRow("adminpermission", new String[]{"permission_id", "permission_name", "resource_type_id"}, new String[]{"4", "'VIEW.USE'", "3"}, false);
        
        dbAccessor.insertRow("adminprivilege", new String[]{"privilege_id", "permission_id", "resource_id", "principal_id"}, new String[]{"1", "1", "1", "1"}, false);
       
        String encodedAdmin = passwordEncoder.encode("admin");
        dbAccessor.insertRow("users", new String[]{"user_id", "active", "ldap_user", "principal_id",  "user_name", "user_password"}, new String[]{"1", "1", "0", "1","'admin'","'"+encodedAdmin+"'"}, false);

        MetainfoEntity schemaVersion = new MetainfoEntity();
        schemaVersion.setMetainfoName(Configuration.SERVER_VERSION_KEY);
        schemaVersion.setMetainfoValue(ambariMetaInfo.getServerVersion());
        metainfoDAO.create(schemaVersion);

    }

}
