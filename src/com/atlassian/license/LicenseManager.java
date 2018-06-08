 package com.atlassian.license;
 
 import com.atlassian.extras.common.log.Logger;
 import com.atlassian.extras.common.log.Logger.Log;
 import com.atlassian.license.decoder.LicenseDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 
 
 
 
 
 
 
 
 
 public class LicenseManager
 {
   private static final Logger.Log log = Logger.getInstance(LicenseManager.class);
   
 
   Map licenseList;
   
 
   Map licenseConfigurations;
   
   private static LicenseManager licenseManager;
   
 
   public LicenseManager()
   {
     this.licenseConfigurations = new HashMap();
     this.licenseList = new HashMap();
   }
   
 
 
 
 
   public static LicenseManager getInstance()
   {
     if (licenseManager == null)
     {
       licenseManager = new LicenseManager();
     }
     
     return licenseManager;
   }
   
   public void addLicenseConfiguration(String applicationName, LicenseTypeStore licenseTypeStore, LicenseRegistry licenseRegistry)
   {
     LicenseConfiguration licenseConfiguration = new LicenseConfiguration(licenseRegistry, licenseTypeStore);
     this.licenseConfigurations.put(applicationName, licenseConfiguration);
   }
   
   public LicenseRegistry getLicenseRegistry(String applicationName)
   {
     return getLicenseConfiguration(applicationName).getLicenseRegistry();
   }
   
   public LicenseTypeStore getLicenseTypeStore(String applicationName)
   {
     return getLicenseConfiguration(applicationName).getLicenseTypeStore();
   }
   
   private LicenseConfiguration getLicenseConfiguration(String applicationName)
   {
     LicenseConfiguration licenseConfiguration = (LicenseConfiguration)this.licenseConfigurations.get(applicationName);
     if (licenseConfiguration == null)
     {
       throw new RuntimeException("No LicenseConfiguration found for key " + applicationName);
     }
     return licenseConfiguration;
   }
   
 
 
 
 
 
   public LicenseTypeStore lookupLicenseTypeStore(String applicationName)
   {
     LicenseConfiguration licenseConfiguration = (LicenseConfiguration)this.licenseConfigurations.get(applicationName);
     if (licenseConfiguration == null)
     {
       return null;
     }
     
 
     return licenseConfiguration.getLicenseTypeStore();
   }
   
 
 //ÐÞ¸ÄByCharlieGao at 20180607
   public boolean hasValidLicense(String licenseKey)
   {
	 if(true) return true;		  
     return (getLicense(licenseKey) != null) && (!getLicense(licenseKey).isExpired());
   }
   
   public License getLicense(String applicationName)
   {
     if ((this.licenseList.isEmpty()) || (!this.licenseList.containsKey(applicationName)))
     {
       try
       {
         License license = null;
         LicenseConfiguration licenseConfiguration = (LicenseConfiguration)this.licenseConfigurations.get(applicationName);
         if (licenseConfiguration == null)
         {
           log.error("There is no License Configuration defined for the application " + applicationName + ".");
           return null;
         }
         LicenseRegistry licenseRegistry = licenseConfiguration.getLicenseRegistry();
         String licenseStr = licenseRegistry.getLicenseMessage();
         String hash = licenseRegistry.getLicenseHash();
         
         if ((licenseStr == null) || (hash == null))
         {
           log.info("There is no license string or hash defined for the application " + applicationName + ".");
           return null;
         }
         
         LicensePair pair = null;
         
         try
         {
           pair = new LicensePair(licenseStr, hash);
         }
         catch (LicenseException e)
         {
           log.error("Could not build a license pair", e);
           
           return null;
         }
         
         license = LicenseDecoder.getLicense(pair, applicationName);
         this.licenseList.put(applicationName, license);
       }
       catch (Exception e)
       {
         log.error("Exception getting license: " + e, e);
       }
     }
     
     return (License)this.licenseList.get(applicationName);
   }
   
 
 
 
 
 
 
 
   public License setLicense(String license, String applicationName)
   {
     LicensePair pair = null;
     
     try
     {
       pair = new LicensePair(license);
       
       License updatedLicense = LicenseDecoder.getLicense(pair, applicationName);
       
       if (LicenseDecoder.isValid(pair, applicationName))
       {
         setLicense(pair, applicationName);
       }
       
       return updatedLicense;
     }
     catch (Exception e)
     {
       log.warn("Attempt to set invalid license. Ensure that you are calling setLicense(license, appName) - not (appName, license)", e);
     }
     return null;
   }
   
   public void setLicense(LicensePair pair, String applicationName)
     throws LicenseException
   {
     if (pair != null)
     {
       this.licenseList.remove(applicationName);
       LicenseConfiguration licenseConfiguration = (LicenseConfiguration)this.licenseConfigurations.get(applicationName);
       LicenseRegistry licenseRegistry = licenseConfiguration.getLicenseRegistry();
       licenseRegistry.setLicenseMessage(LicenseUtils.getString(pair.getLicense()));
       licenseRegistry.setLicenseHash(LicenseUtils.getString(pair.getHash()));
     }
   }
   
 
 
 
   public LicensePair getLicensePair(String applicationName)
   {
     try
     {
       LicenseConfiguration licenseConfiguration = (LicenseConfiguration)this.licenseConfigurations.get(applicationName);
       LicenseRegistry licenseRegistry = licenseConfiguration.getLicenseRegistry();
       return new LicensePair(licenseRegistry.getLicenseMessage(), licenseRegistry.getLicenseHash());
     }
     catch (LicenseException e)
     {
       log.error("Couldn't get the LicensePair ...", e); }
     return null;
   }
   
   public LicenseType getLicenseType(String applicationName, String licenseTypeString)
     throws LicenseException
   {
     LicenseConfiguration licenseConfiguration = (LicenseConfiguration)this.licenseConfigurations.get(applicationName);
     return licenseConfiguration.getLicenseTypeStore().getLicenseType(licenseTypeString);
   }
   
   public LicenseType getLicenseType(String applicationName, int licenseTypeCode) throws LicenseException
   {
     LicenseConfiguration licenseConfiguration = (LicenseConfiguration)this.licenseConfigurations.get(applicationName);
     return licenseConfiguration.getLicenseTypeStore().getLicenseType(licenseTypeCode);
   }
   
   public void reset()
   {
     this.licenseConfigurations.clear();
     this.licenseList.clear();
     licenseManager = null;
   }
   
   public void clearLicenseConfigurations()
   {
     this.licenseConfigurations.clear();
   }
   
   public void removeLicense(String applicationName)
   {
     this.licenseList.remove(applicationName);
   }
 }


/* Location:              E:\Software\jira\atlassian-jira\WEB-INF\atlassian-bundled-plugins\atlassian-universal-plugin-manager-plugin-2.22.9.jar!\com\atlassian\license\LicenseManager.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */