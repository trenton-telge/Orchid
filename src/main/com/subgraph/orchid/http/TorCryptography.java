package com.subgraph.orchid.http;

import com.subgraph.orchid.logging.Logger;
import java.lang.reflect.Field;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Map;
import javax.crypto.Cipher;

public class TorCryptography {
    private static final Logger logger = Logger.getInstance(TorCryptography.class);
    
    public static boolean hasRestrictedCryptography(){
	try {
            if(Cipher.getMaxAllowedKeyLength("AES") < 256) {
                return true;
            }
        } catch (Exception e) {
            // Assume unrestricted if we can not determine and let Orchid throw an error.
        }
        return false;
    }

    private static boolean isOracleJRE() {
        // This simply matches the Oracle JRE, but not OpenJDK.
        return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
    }
    
    public static void removeCryptographyRestrictions() {
        if(hasRestrictedCryptography() && isOracleJRE()){
            try {
                // http://stackoverflow.com/a/22492582
                final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
                final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
                final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

                final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
                isRestrictedField.setAccessible(true);
                isRestrictedField.set(null, false);

                final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
                defaultPolicyField.setAccessible(true);
                final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

                final Field perms = cryptoPermissions.getDeclaredField("perms");
                perms.setAccessible(true);
                ((Map<?, ?>) perms.get(defaultPolicy)).clear();

                final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
                instance.setAccessible(true);
                defaultPolicy.add((Permission) instance.get(null));
            } catch (final Exception e) {
                logger.error("Unable to bypass JCE Unlimited Strength Jurisdiction policy files.", e);
            }
        }
    }
}