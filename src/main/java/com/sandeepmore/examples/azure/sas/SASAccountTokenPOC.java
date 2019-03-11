/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sandeepmore.examples.azure.sas;

import com.microsoft.azure.storage.blob.AccountSASResourceType;
import com.microsoft.azure.storage.blob.AccountSASService;
import com.microsoft.azure.storage.blob.AccountSASSignatureValues;
import com.microsoft.azure.storage.blob.AnonymousCredentials;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.SASQueryParameters;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse;
import io.reactivex.Single;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.Locale;

public class SASAccountTokenPOC {

  public static final String ACCOUNT_NAME = "AZURE_STORAGE_ACCOUNT";
  public static final String ACCOUNT_KEY = "AZURE_STORAGE_ACCESS_KEY";

  public static final String CONTAINER_NAME = "STORAGE_CONTAINER_NAME";
  public static final String BLOB_NAME = "STORAGE_BLOB_NAME";

  private static final OffsetDateTime TOKEN_EXPIRY_TIME = OffsetDateTime.now().plusDays(1);

  private static final String URL_FORMAT_ACCOUNT = "https://%s.blob.core.windows.net%s";
  //private static final String URL_FORMAT_CONTAINER = "https://%s.blob.core.windows.net/%s%s";

  /**
   * Temp container to be created by the test
   */
  private static final String TEST_CONTAINER_NAME = "myexampletestcontainer"+System.currentTimeMillis();

  /**
   * Temp blob name
   */
  private static final String TEST_BLOB_NAME = "temp.txt";

  private static final String accountName = System.getenv(ACCOUNT_NAME);
  private static final String accountKey = System.getenv(ACCOUNT_KEY);
  private static final String containerName = System.getenv(CONTAINER_NAME);
  private static final String blobName = System.getenv(BLOB_NAME);

  /**
   * Temp file to be uploaded and downloaded by the test
   */
  private static File tempFile = null;


  public static void main(final String ...strings) throws InvalidKeyException {

    try {
      tempFile = Util.createTempFile();
    } catch (IOException e) {
      System.out.println("Error creating temp file");
      e.printStackTrace();
    }

    try {
      createContainer();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

  }

  // FIXME: Fix the issues with create container.
  private static void createContainer()
      throws InvalidKeyException, MalformedURLException {

    /* Set the desired SAS signature values */
    final AccountSASSignatureValues values = Util.getAccountSignatureValues(
        TOKEN_EXPIRY_TIME);

    /* Set the desired permissions */
    final String permissions = Util.getPermissions(true, true, true,true,true,true);
    final String sasAccountUrl = getSASTokenForAccount(accountName, accountKey, values, permissions, false, true);

    System.out.println(">>>> SAS Account Token URL: "+sasAccountUrl);

    final ServiceURL serviceURL = new ServiceURL(new URL(sasAccountUrl),
        StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

    final ContainerURL containerURL = serviceURL.createContainerURL(TEST_CONTAINER_NAME);

    //BlockBlobURL blobURL = containerURL.createBlockBlobURL(TEST_BLOB_NAME);

    //final ContainerURL containerURL =  new ContainerURL(new URL(sasAccountUrl), StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

    Single<ContainerCreateResponse> response = containerURL.create();

    System.out.println(response.blockingGet().toString());


  }


  /**
   * Return SAS Account token URL
   * @param values
   * @param permissions
   * @return
   * @throws InvalidKeyException
   */
  private static String getSASTokenForAccount(final String accountName, final String accountKey, final AccountSASSignatureValues values, final String permissions, final boolean isServiceResource, final boolean isBlobResource )
      throws InvalidKeyException {
    // create a credential object
    final SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);
    values.withPermissions(permissions);


    final AccountSASService service = new AccountSASService()
        .withBlob(isBlobResource);
    values.withServices(service.toString());


    final AccountSASResourceType resourceType = new AccountSASResourceType()
        .withContainer(true);
        //.withObject(isBlobResource)
        //.withService(isServiceResource);
    values.withResourceTypes(resourceType.toString());

    final SASQueryParameters params = values.generateSASQueryParameters(credential);
    final String encodeParams = params.encode();

    final String accountSasUrl = String.format(Locale.ROOT, URL_FORMAT_ACCOUNT, accountName, encodeParams);

    return accountSasUrl;
  }

}
