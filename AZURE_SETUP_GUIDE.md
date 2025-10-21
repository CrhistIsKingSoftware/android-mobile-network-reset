# Azure Blob Storage Setup Guide

This guide will help you set up free Azure Blob Storage for automatic log uploads from the Mobile Network Reset app.

## Why Azure?

Azure offers a **free tier** with:
- 5GB storage (plenty for logs)
- 20,000 read/write operations per month
- No credit card required for the free tier
- Reliable and secure cloud storage

## Step-by-Step Setup

### 1. Create a Free Azure Account

1. Visit [Azure Portal](https://portal.azure.com)
2. Click **"Start free"** or **"Sign in"**
3. Sign in with your Microsoft account (or create one)
4. Complete the registration process
   - You may need to verify your phone number
   - A credit card is required to verify identity, but you won't be charged for the free tier

### 2. Create a Storage Account

1. Once logged into Azure Portal, click **"Create a resource"**
2. Search for **"Storage account"** and select it
3. Click **"Create"**
4. Fill in the details:
   - **Subscription**: Select your subscription (should be "Azure subscription 1" for free accounts)
   - **Resource group**: Create a new one (e.g., "mobile-network-reset-rg")
   - **Storage account name**: Choose a unique name (e.g., "mobilenetworklogs123")
     - Must be lowercase letters and numbers only
     - Must be globally unique
   - **Region**: Choose the closest region to you
   - **Performance**: Standard
   - **Redundancy**: LRS (Locally-redundant storage) - this is the cheapest option
5. Click **"Review + create"** and then **"Create"**
6. Wait for deployment to complete (usually takes 1-2 minutes)

### 3. Create a Container

1. Once deployment is complete, click **"Go to resource"**
2. In the left menu, under **"Data storage"**, click **"Containers"**
3. Click **"+ Container"** at the top
4. Enter container details:
   - **Name**: Choose a name (e.g., "mobile-network-reset-logs")
     - Must be lowercase letters, numbers, and hyphens only
   - **Public access level**: Private (no anonymous access)
5. Click **"Create"**

### 4. Generate a SAS Token

A SAS (Shared Access Signature) token allows the app to upload logs securely without exposing your account key.

1. Open your newly created container
2. In the left menu, click **"Shared access signature"**
3. Configure the SAS token:
   - **Permissions**: Check **Write**, **Add**, and **Create** (uncheck Read, Delete, List)
   - **Start date/time**: Set to current date/time
   - **Expiry date/time**: Set to 1 year from now (or your preferred duration)
   - **Allowed IP addresses**: Leave blank (or add your IP for extra security)
   - **Allowed protocols**: HTTPS only
4. Click **"Generate SAS token and URL"**
5. **Copy the SAS token** (it starts with "?sv=...")
   - Save this token securely - you'll need it to configure the app
   - You won't be able to see it again after closing this page

### 5. Configure the App

1. Open the **Mobile Network Reset** app on your Android device
2. Tap **"View Logs"** button on the main screen
3. Tap **"Configure Cloud"** button
4. Enter the following information:
   - **Storage Account Name**: The name you chose in step 2 (e.g., "mobilenetworklogs123")
   - **Container Name**: The name you chose in step 3 (e.g., "mobile-network-reset-logs")
   - **SAS Token**: Paste the SAS token you copied in step 4 (include the "?" at the beginning)
5. Tap **"Save"**

### 6. Test the Upload

1. After configuration, tap **"Upload"** to test the connection
2. You should see a success message
3. To verify in Azure Portal:
   - Go to your container in Azure Portal
   - You should see a new blob (file) with the uploaded logs
   - Blob name format: `android_[DeviceModel]_[Timestamp]_all_logs_[Timestamp].txt`

## Optional: Enable Auto-Upload

The app automatically enables auto-upload when you configure cloud storage. Logs will be uploaded:
- Every 24 hours when the app is opened
- Immediately when a crash is detected

You can manually upload logs at any time by:
1. Opening the app
2. Tapping **"View Logs"**
3. Tapping **"Upload"**

## Viewing Uploaded Logs

1. Go to Azure Portal
2. Navigate to your Storage Account
3. Click on **"Containers"**
4. Click on your container name
5. You'll see a list of uploaded log files
6. Click on any file to view options:
   - Download to view locally
   - View properties
   - Delete if no longer needed

## Cost Considerations

Azure's free tier includes:
- **Storage**: First 5GB free
- **Transactions**: 20,000 write operations per month free
- **Data egress**: First 100GB per month free

For typical usage with the Mobile Network Reset app:
- Each log upload is ~100KB - 1MB
- Daily auto-upload = ~30 uploads per month
- This is well within the free tier limits

## Security Best Practices

1. **SAS Token Security:**
   - Never share your SAS token publicly
   - Set appropriate expiry dates (recommended: 6-12 months)
   - Use write-only permissions (don't enable Read or List)
   - Regenerate tokens periodically

2. **Container Access:**
   - Keep container access level as "Private"
   - Only use SAS tokens for app access
   - Don't make the container publicly accessible

3. **Monitor Usage:**
   - Check Azure Portal regularly for unusual activity
   - Review uploaded logs periodically
   - Delete old logs if storage approaches 5GB limit

## Troubleshooting

### Issue: "Storage account name already exists"
**Solution**: The name must be globally unique. Try adding numbers or your name to make it unique.

### Issue: "Failed to upload logs"
**Solutions:**
- Verify the storage account name is correct
- Ensure the container exists
- Check that the SAS token hasn't expired
- Verify the SAS token has write permissions
- Check your internet connection

### Issue: "Response code: 403 Forbidden"
**Solutions:**
- SAS token doesn't have required permissions (add Write, Add, Create)
- SAS token has expired - generate a new one
- Check that you copied the entire SAS token including "?"

### Issue: "Response code: 404 Not Found"
**Solutions:**
- Container name is incorrect - check spelling and case
- Container doesn't exist - create it in Azure Portal
- Storage account name is incorrect

## Alternative: Manual Log Sharing

If you don't want to set up cloud storage, you can still:
1. Tap **"View Logs"** in the app
2. Tap **"Share"** to share logs via:
   - Email
   - Google Drive
   - Messaging apps
   - Any app that supports file sharing

## Additional Resources

- [Azure Free Account Documentation](https://azure.microsoft.com/en-us/free/)
- [Azure Blob Storage Documentation](https://docs.microsoft.com/en-us/azure/storage/blobs/)
- [SAS Token Documentation](https://docs.microsoft.com/en-us/azure/storage/common/storage-sas-overview)

## Need Help?

If you encounter issues:
1. Check the [LOGGING_ENHANCEMENTS.md](LOGGING_ENHANCEMENTS.md) documentation
2. Review the troubleshooting section above
3. Open an issue on GitHub with:
   - Steps you followed
   - Error messages received
   - Screenshots (hide sensitive information)
