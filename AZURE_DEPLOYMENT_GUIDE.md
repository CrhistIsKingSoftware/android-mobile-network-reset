# Azure Deployment Guide for Cloud Logging

This guide explains how to set up Azure Blob Storage for automatic log uploads and configure GitHub Actions to deploy the app with cloud logging enabled.

## Overview

The Mobile Network Reset app is configured to automatically upload logs to Azure Blob Storage when built with the proper credentials. The Azure credentials are injected at build time via environment variables and stored in the APK's BuildConfig, eliminating the need for end-users to configure cloud storage.

## Prerequisites

- Azure account (free tier is sufficient)
- GitHub repository with Actions enabled
- Repository maintainer/admin access

## Step 1: Create Azure Storage Account

### 1.1 Sign in to Azure Portal

1. Go to [Azure Portal](https://portal.azure.com)
2. Sign in with your Microsoft account

### 1.2 Create Storage Account

1. Click **"Create a resource"**
2. Search for **"Storage account"** and select it
3. Click **"Create"**
4. Fill in the details:
   - **Subscription**: Your Azure subscription
   - **Resource group**: Create new (e.g., "mobile-network-reset-rg")
   - **Storage account name**: Choose unique name (e.g., "mobilenetworklogs")
     - Must be lowercase letters and numbers only
     - Must be globally unique
   - **Region**: Choose closest region
   - **Performance**: Standard
   - **Redundancy**: LRS (Locally-redundant storage)
5. Click **"Review + create"** then **"Create"**
6. Wait for deployment to complete

## Step 2: Create Container

1. Go to your storage account
2. In the left menu, click **"Containers"** under "Data storage"
3. Click **"+ Container"**
4. Enter:
   - **Name**: "mobile-network-reset-logs" (or your preferred name)
   - **Public access level**: Private
5. Click **"Create"**

## Step 3: Generate SAS Token

A SAS (Shared Access Signature) token provides secure, time-limited access to your storage.

1. In your container, click **"Shared access signature"** in left menu
2. Configure permissions:
   - **Permissions**: Check **Write**, **Add**, and **Create** only
   - **Start date/time**: Current date/time
   - **Expiry date/time**: 1-2 years from now
   - **Allowed protocols**: HTTPS only
3. Click **"Generate SAS token and URL"**
4. **Copy the SAS token** (starts with "?sv=...")
   - Save this securely - you'll need it for GitHub Secrets
   - Do NOT copy the full URL, just the token part

## Step 4: Configure GitHub Secrets

GitHub Secrets allow you to securely store the Azure credentials and inject them during builds.

### 4.1 Add Secrets to Repository

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**
4. Add the following secrets (one at a time):

**Secret 1:**
- Name: `AZURE_STORAGE_ACCOUNT`
- Value: Your storage account name (e.g., "mobilenetworklogs")

**Secret 2:**
- Name: `AZURE_STORAGE_CONTAINER`
- Value: Your container name (e.g., "mobile-network-reset-logs")

**Secret 3:**
- Name: `AZURE_STORAGE_SAS_TOKEN`
- Value: Your SAS token (including the "?" at the start)

### 4.2 Verify Secrets

After adding all three secrets, you should see:
- `AZURE_STORAGE_ACCOUNT`
- `AZURE_STORAGE_CONTAINER`
- `AZURE_STORAGE_SAS_TOKEN`

## Step 5: Build and Deploy

Once the secrets are configured, the GitHub Actions workflows will automatically inject them during builds.

### Automatic Builds

The CI/CD workflows are already configured to use these secrets:

**CI Builds (on push/PR):**
- Triggered automatically on push or pull request
- Uses secrets from repository settings
- Builds APK with cloud logging enabled

**Release Builds (on tag):**
- Triggered when you push a version tag
- Uses secrets from repository settings
- Creates release APK with cloud logging enabled

### Manual Build

If building locally and want cloud logging enabled:

```bash
export AZURE_STORAGE_ACCOUNT="your-account-name"
export AZURE_STORAGE_CONTAINER="your-container-name"
export AZURE_STORAGE_SAS_TOKEN="your-sas-token"
./gradlew assembleRelease
```

## How It Works

### Build-Time Injection

The `app/build.gradle` file contains:

```gradle
buildConfigField "String", "AZURE_STORAGE_ACCOUNT", "\"${System.getenv('AZURE_STORAGE_ACCOUNT') ?: ''}\""
buildConfigField "String", "AZURE_STORAGE_CONTAINER", "\"${System.getenv('AZURE_STORAGE_CONTAINER') ?: 'mobile-network-reset-logs'}\""
buildConfigField "String", "AZURE_STORAGE_SAS_TOKEN", "\"${System.getenv('AZURE_STORAGE_SAS_TOKEN') ?: ''}\""
```

This reads environment variables and embeds them in the APK's `BuildConfig` class.

### Runtime Usage

The app checks if credentials are configured:

```kotlin
fun isConfigured(): Boolean {
    return BuildConfig.AZURE_STORAGE_ACCOUNT.isNotEmpty() &&
           BuildConfig.AZURE_STORAGE_CONTAINER.isNotEmpty() &&
           BuildConfig.AZURE_STORAGE_SAS_TOKEN.isNotEmpty()
}
```

If configured, logs are automatically uploaded every 24 hours and immediately on crashes.

## Verification

### Check Build Logs

After pushing code or creating a release:
1. Go to **Actions** tab in GitHub
2. Click on the latest workflow run
3. Check that build completed successfully
4. Download the APK artifact

### Test Upload

1. Install the built APK on a device
2. Open the app and use it normally
3. Go to **View Logs** → **Upload to Cloud**
4. Check Azure Portal to see if logs appear in your container

### View Uploaded Logs

1. Go to Azure Portal → Your Storage Account → Containers
2. Click on your container
3. You should see uploaded log files with names like:
   - `android_[DeviceModel]_[Timestamp]_all_logs_[Timestamp].txt`

## Security Considerations

### SAS Token Security

1. **Limited Permissions**: Only grant Write, Add, Create (not Read or Delete)
2. **Expiry Date**: Set reasonable expiry (1-2 years maximum)
3. **Regular Rotation**: Regenerate tokens periodically
4. **Secret Storage**: Never commit tokens to repository

### GitHub Secrets Security

1. **Access Control**: Only repository admins can view/edit secrets
2. **Audit Logs**: GitHub logs all secret access
3. **Branch Protection**: Secrets only available to protected branches
4. **Fork Protection**: Secrets not available to forked repositories

### Container Security

1. **Private Access**: Keep container access level as "Private"
2. **HTTPS Only**: Enforce HTTPS-only access
3. **Monitoring**: Review access logs regularly
4. **Data Retention**: Implement lifecycle policies to delete old logs

## Cost Management

### Azure Free Tier

The free tier includes:
- **Storage**: 5GB free
- **Transactions**: 20,000 write operations/month free
- **Data Transfer**: 100GB egress/month free

### Estimating Usage

For typical app usage:
- **Log size**: ~100KB - 1MB per upload
- **Frequency**: Once per day per device
- **Monthly cost**: Typically $0 (within free tier)

### Monitoring Usage

1. Go to Azure Portal → Your Storage Account
2. Click **"Metrics"** to view usage
3. Set up alerts if approaching limits

## Troubleshooting

### Build Fails with "BuildConfig not found"

**Solution**: Make sure `buildConfig true` is enabled in `app/build.gradle`:

```gradle
buildFeatures {
    viewBinding true
    buildConfig true
}
```

### Logs Not Uploading

**Check 1**: Verify secrets are set in GitHub
**Check 2**: Rebuild with fresh secrets
**Check 3**: Check device logs for error messages
**Check 4**: Verify SAS token hasn't expired

### "Upload" Button Disabled

This means the app was built without Azure credentials. Build again with proper environment variables or GitHub secrets configured.

### 403 Forbidden Error

**Causes**:
- SAS token expired
- SAS token missing Write/Add/Create permissions
- Container access policy changed

**Solution**: Generate new SAS token with proper permissions

### 404 Not Found Error

**Causes**:
- Container name incorrect
- Storage account name incorrect
- Container deleted

**Solution**: Verify container exists and names match

## Updating Credentials

### Rotating SAS Token

When your SAS token is about to expire:

1. Generate new SAS token in Azure Portal (Step 3)
2. Update `AZURE_STORAGE_SAS_TOKEN` secret in GitHub
3. Trigger new build (push code or create release)
4. Old APKs will continue using old token until it expires
5. New APKs will use new token

### Changing Container

To use a different container:

1. Create new container in Azure Portal
2. Update `AZURE_STORAGE_CONTAINER` secret in GitHub
3. Trigger new build

## Best Practices

1. **Token Rotation**: Rotate SAS tokens every 6-12 months
2. **Access Review**: Regularly review who has access to secrets
3. **Monitoring**: Set up alerts for unusual upload patterns
4. **Backup**: Keep backup of container data if needed
5. **Documentation**: Document any changes to configuration

## Support

If you encounter issues:
1. Check GitHub Actions logs for build errors
2. Check device logcat for runtime errors
3. Verify Azure Portal for storage account status
4. Open GitHub issue with error details

## Additional Resources

- [Azure Storage Documentation](https://docs.microsoft.com/en-us/azure/storage/)
- [GitHub Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [SAS Token Documentation](https://docs.microsoft.com/en-us/azure/storage/common/storage-sas-overview)
