README
======

This README explains the installation and configuration of a custom
authentication module for OpenAM 12.x or 13.x  in a RSA ClearTrust/
Access Manager environment. This custom authentication enables SSO from
ClearTrust to OpenAM.

What is in this zip file:
========================
This zip file contains following components.

    1. src/main/resources - contains files for registeration of the custom auth module
    2. src - This directory contains source for custom auth module.
    3. common.cfg, compile.sh and test.sh - scripts for compiling and testing the auth
       module. Edit common.cfg for your enviorment before compiling and testing.

    Note: The ClearTrust SDK jar files are not included in this repository due to
          licensing restrictions.



Pre-requisites :
================

   1. RSA ClearTurst server X.X or higer version installed and configured.
   2. RSA ClearTurst Runtime SDK X.X or higher version installed and configured.
   3. openam.war from OpenAM Distribution Kit

Required SSO integration components:
===================================

   1. OpenAM 12.x or 13.x
   2. A web container preferably Tomcat 8.x.
   3. Custom authentication module components which is bundled in this zip file.


OpenAM Installation and Configuration:
=======================================

  1. Create a temporary directory /export/tmp and explode the openam.war
     using jar -xvf openam.war.
     From now on, /export/tmp is called as a war staging area and is
     represented with a marco $WAR_DIR

  2. Copy build/ctauthmodule.jar to $WAR_DIR/WEB-INF/lib

  3. Copy resources/CTAuthService.properties to $WAR_DIR/WEB-INF/classes

  4. Copy resources/CTAuthService.xml to $WAR_DIR/config/auth/default and
     also to the directory $WAR_DIR/config/auth/default_en

  5. Re-war openam.war using jar cvf openam.war from $WAR_DIR

  6. Deploy openam.war onto OpenAM web container. The deployment is self
     explanatory. Please check the web container documentation for war
     deployment.

  7. Access the deployed application using
      http://host:port/openam

  8. Accessing deployed application redirects to openam configurator.
     Choose custom configuration. By default OpenAM uses embedded directory
     server for configuration, however, you could choose to use existing
     or a new directory server instance for configuration.

     Note: OpenAM can be configured to use various user repository for
     validating the user existance, however, you could also choose to ignore profile.

  9. After successful configuration, the configuration redirects to the login page


Auth module configuration:
==========================

Now we have to load the RSA ClearTurst authentication module service into
OpenAM and configure for the SSO integration. The auth module service
is loaded from a OpenAM command line utility called as "ssoadm". For OpenAM,
the ssoadm utility is also exposed as ssoadm.jsp.

If you want to use the commandline then refer to the following URL and skip
step 1-5
https://backstage.forgerock.com/#!/docs/openam/12.0.0/dev-guide#chap-auth-spi

Here we will use use browser based ssoadm.jsp for OpenAM configuration
changes.

  1. Login into OpenAM using amadmin and enable ssoadm.jsp as follows
     https://wikis.forgerock.org/confluence/display/openam/Activate+ssoadm.jsp

  2. Now access the following URL provided that you have
     http://host:port/openam/ssoadm.jsp

  3. Choose "create-svc" option.

  4. Copy and paste the xml file from resources/CTAuthService.xml and Submit
     This will define the auth module service into OpenAM configuration.

  5. Now register the auth module into the authentication core framework.

     http://host:port/openam/ssoadm.jsp
     Choose "register-auth-module option".
     Enter "org.forgerock.openam.authentication.modules.cleartrust.CTAuth" as the
     auth module class name.


  6. Now verify that the auth module is registered to the default realm.
     http://host:port/openam, click on default realm, and click on
     "authentication" tab, click "New", you should see "RSA ClearTrust" in the
     list of modules.

  7. Click on "RSA ClearTrust" and create a new module instance called "CTAuth"

  8. Once the instance is created go back to Authentication and click on "CTAuth"
     to configure it. Fill in the form and save.


Testing:
=======

The testing of the module  assumes that ClearTrust SDK is already
installed and configured. Please check the ClearTrust documentation
for ClearTrust SDK installation.

CAUTION: All the runtime associated jar files should be copied to
OpenAM WEB-INF/lib directory or in the classpath of OpenAM.  The
path is usually sdk/java/runtime/lib.


1. Now access the ClearTrust protected application and login with
   ClearTrust configured user to establish CTSESSION. The configuration
   of ClearTrust policy and authentication schemes are outside scope of this
   documentation and please check ClearTrust documentation for more
   information.

2. After successful authentication at ClearTrust server, access the OpenAM
   auth module url as follows:

   http://host:port/openam/XUI/#login/&module=CTAuth or if using Legacy UI
   http://host:port/openam/UI/Login?module=CTAuth

   This should provide a valid OpenAM session.

   Note: Assumption here is that ClearTrust and OpenAM are in the same
         physical domain.

   By default OpenAM authentication framework looks for user profile existance
   in it's known data repositories. However, you could use ignoreProfile
   option if your integration does not require a user to be searched from
   ClearTrust's user repository. Check the OpenAM documentation for more info
   about ignoreProfile option.
