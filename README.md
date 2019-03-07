# Azure SAS Token POC

This is a POC that demonstrates how to generate and consume [Azure AD SAS](https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1) Tokens.
This POC uses [Azure Storage SDK](https://github.com/Azure/azure-storage-java) for Java.

## Setup

### Create a Storage account
* You need an Azure AD Subscription, [create an account](https://azure.microsoft.com/free/?WT.mc_id=A261C142F) if you don't have one. 
* Create a new storage account
    * Go to the [Azure portal](https://portal.azure.com) and log in using your Azure account. 
    * On the Hub menu, select **New** > **Storage** > **Storage account - blob, file, table, queue**. 
    * Enter a name for your storage account. The name must be between 3 and 24 characters in length and may contain numbers and lowercase letters only. It must also be unique.
    * Set `Deployment model` to **Resource manager**.
    * Set `Account kind` to **General purpose**.
    * Set `Performance` to **Standard**. 
    * Set `Replication` to **Locally Redundant storage (LRS)**.
    * Set `Storage service encryption` to **Disabled**.
    * Set `Secure transfer required` to **Disabled**.
    * Select your subscription. 
    * For `resource group`, create a new one and give it a unique name. 
    * Select the `Location` to use for your storage account.
    * Check **Pin to dashboard** and click **Create** to create your storage account. 

Click on it to open it. Under SETTINGS, click **Access keys**. Select a key and copy the **key1** to the clipboard, then save it for later use.

### Set environment variable

Linux/MacOS

```
export AZURE_STORAGE_ACCOUNT="<youraccountname>"
export AZURE_STORAGE_ACCESS_KEY="<youraccountkey>"
export STORAGE_CONTAINER_NAME="<yourcontainername>"
export STORAGE_BLOB_NAME="<yourblobname>"
```

Windows

```
setx AZURE_STORAGE_ACCOUNT "<youracountname>"
setx AZURE_STORAGE_ACCESS_KEY "<youraccountkey>"
setx STORAGE_CONTAINER_NAME "<yourcontainername>"
setx STORAGE_BLOB_NAME "<yourblobname>"
```


## Build / Run

To build and run execute the command `mvn compile exec:java` this should run the POC.

## Reference
* [storage-blobs-java-v10-quickstart](https://github.com/Azure-Samples/storage-blobs-java-v10-quickstart/blob/master/README.md)
* [Azure Storage SDK v10 for Java](https://github.com/azure/azure-storage-java/tree/vNext)