package com.subgraph.orchid.directory;

import com.subgraph.orchid.DirectoryServer;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.RouterStatus;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DirectoryServerImpl extends RouterImpl implements DirectoryServer {
    private static final Logger logger = Logger.getInstance(DirectoryServerImpl.class);
	
    private final List<KeyCertificate> certificates = new ArrayList<>();

    private boolean isHiddenServiceAuthority = false;
    private boolean isBridgeAuthority = false;
    private boolean isExtraInfoCache = false;
    private int port;
    private HexDigest v3Ident;

    DirectoryServerImpl(RouterStatus status) {
        super(null, status);
    }

    void setHiddenServiceAuthority() { isHiddenServiceAuthority = true; }
    void unsetHiddenServiceAuthority() { isHiddenServiceAuthority = false; }
    void setBridgeAuthority() { isBridgeAuthority = true; }
    void setExtraInfoCache() { isExtraInfoCache = true; }
    void setPort(int port) { this.port = port; }
    void setV3Ident(HexDigest fingerprint) { this.v3Ident = fingerprint; }

    public boolean isTrustedAuthority() {
            return true;
    }

    /**
     * Return true if this DirectoryServer entry has
     * complete and valid information.
     */
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isV2Authority() {
        return hasFlag("Authority") && hasFlag("V2Dir");
    }

    @Override
    public boolean isV3Authority() {
        return hasFlag("Authority") && v3Ident != null;
    }

    @Override
    public boolean isHiddenServiceAuthority() {
        return isHiddenServiceAuthority;
    }

    @Override
    public boolean isBridgeAuthority() {
        return isBridgeAuthority;
    }

    @Override
    public boolean isExtraInfoCache() {
        return isExtraInfoCache;
    }

    @Override
    public HexDigest getV3Identity() {
        return v3Ident;
    }

    /**
     * https://github.com/geo-gs/Orchid/commit/22beeae1b881707491addaba6a7654e9de9f9db1
     */
    @Override
    public KeyCertificate getCertificateByAuthority(HexDigest fingerprint){

        for(KeyCertificate kc: getCertificates()) {
            if(kc.getAuthorityFingerprint().equals(fingerprint)) {
                return kc;
            }
        }
        return null;            
    }

    @Override
    public KeyCertificate getCertificateByFingerprint(HexDigest fingerprint) {
        for(KeyCertificate kc: getCertificates()) {
            if(kc.getAuthoritySigningKey().getFingerprint().equals(fingerprint)) {
                return kc;
            }
        }
        return null;
    }

    @Override
    public List<KeyCertificate> getCertificates() {
        synchronized(certificates) {
            purgeExpiredCertificates();
            purgeOldCertificates();
            return new ArrayList<>(certificates);
        }
    }

    private void purgeExpiredCertificates() {
        certificates.removeIf(KeyCertificate::isExpired);
    }

    private void purgeOldCertificates() {
        if(certificates.size() < 2) {
            return;
        }
        final KeyCertificate newest = getNewestCertificate();
        certificates.removeIf(elem -> elem != newest && isMoreThan48HoursOlder(newest, elem));
    }

    private KeyCertificate getNewestCertificate() {
        KeyCertificate newest = null;
        for(KeyCertificate kc : certificates) {
            if(newest == null || getPublishedMilliseconds(newest) > getPublishedMilliseconds(kc)) {
                newest = kc;
            }
        }
        return newest;
    }

    private boolean isMoreThan48HoursOlder(KeyCertificate newer, KeyCertificate older) {
        final long milliseconds = 48 * 60 * 60 * 1000;
        return (getPublishedMilliseconds(newer) - getPublishedMilliseconds(older)) > milliseconds;
    }

    private long getPublishedMilliseconds(KeyCertificate certificate) {
        return certificate.getKeyPublishedTime().getDate().getTime();
    }

    @Override
    public void addCertificate(KeyCertificate certificate) {
        logger.debug(certificate.getAuthorityFingerprint().toString()+"-"+certificate.getAuthoritySigningKey().toString());
        logger.debug("addCertificate");
        if(!certificate.getAuthorityFingerprint().equals(v3Ident)) {
                throw new IllegalArgumentException("This certificate does not appear to belong to this directory authority");
        }
        synchronized(certificates) {
                certificates.add(certificate);
        }
    }

    @Override
    public String toString() {
        if(v3Ident != null) {
            return "(Directory: "+ getNickname() +" "+ getAddress() +":"+ port +" fingerprint="+ getIdentityHash() +" v3ident="+ 
                    v3Ident +")";
        } else{
            return "(Directory: "+ getNickname() +" "+ getAddress() +":"+ port +" fingerprint="+ getIdentityHash() +")";
        }
    }
}
