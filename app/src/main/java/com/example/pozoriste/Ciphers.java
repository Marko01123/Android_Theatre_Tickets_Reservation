package com.example.pozoriste;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Ciphers {
    /*
    * Generise se AES kljuc i smesta se u AndroidKeyStore. Njime sifrujemo podatke u aplikaciji koje smatramo tajnim.
    * */
    public static SecretKey getAESKey(){
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if(keyStore.containsAlias("kljucZaSifrovanjePodataka")){
                 return (SecretKey) keyStore.getKey("kljucZaSifrovanjePodataka", null);
            } else {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder("kljucZaSifrovanjePodataka",
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setKeySize(256)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(false)
                        .build();
                keyGenerator.init(keySpec);
                return keyGenerator.generateKey();
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException |
                 UnrecoverableKeyException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Nije uspelo pravljenje/dohvatanje kljuca "+e);
        }
    }

    /*
    * Generise se nonce, random vrednost koja se dodaje na lozinku i pravi se hash koji se kasnije upisuje u bazu podataka
    * */
    public byte[] gen_nonce(){
        SecureRandom sr = new SecureRandom();
        byte[] nonceBytes = new byte[8];
        sr.nextBytes(nonceBytes);
        return nonceBytes;
    }

    public String gen_hash(byte[] nonce, String password){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(nonce);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    Metode za sifrovanje i desifrovanje AES algoritmom. Nakon sifrovanja, random generisani IV smestamo u niz bajtova zajedno sa sifratom
    koji se izvlaci u metodi za desifrovanje za svaki sifrat i koristi se pri desifrovanju.
     */
    public byte[] encryptAES(byte[] plaintext, SecretKey kljuc){
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding");
            c.init(Cipher.ENCRYPT_MODE, kljuc);
            AlgorithmParameters params = c.getParameters();
            byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = c.doFinal(plaintext);
            ByteBuffer buffer = ByteBuffer.allocate(ivBytes.length + ciphertext.length);
            buffer.put(ivBytes);
            buffer.put(ciphertext);
            return buffer.array();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | InvalidParameterSpecException e) {
            throw new RuntimeException("Nije uspela enkripcija: "+e);
        }
    }

    public byte[] decryptAES(byte[] encryptedData, SecretKey kljuc){
        try {
            byte[] ivBytes = Arrays.copyOfRange(encryptedData, 0, 16);
            byte[] cipertext = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding");
            c.init(Cipher.DECRYPT_MODE, kljuc, iv);
            return c.doFinal(cipertext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Nije uspela dekripcija: "+e);
        }
    }
}
