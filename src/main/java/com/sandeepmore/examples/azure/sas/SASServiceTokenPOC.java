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

import com.microsoft.azure.storage.blob.AnonymousCredentials;
import com.microsoft.azure.storage.blob.BlobURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.SASQueryParameters;
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.Locale;

import static java.lang.System.exit;

public class SASServiceTokenPOC {

  public static final String ACCOUNT_NAME = "AZURE_STORAGE_ACCOUNT";
  public static final String ACCOUNT_KEY = "AZURE_STORAGE_ACCESS_KEY";

  public static final String CONTAINER_NAME = "STORAGE_CONTAINER_NAME";
  public static final String BLOB_NAME = "STORAGE_BLOB_NAME";

  private static final OffsetDateTime TOKEN_EXPIRY_TIME = OffsetDateTime.now()
      .plusDays(1);

  private static final String URL_FORMAT_ACCOUNT = "https://%s.blob.core.windows.net?%s";
  private static final String URL_FORMAT_BLOB = "https://%s.blob.core.windows.net/%s/%s%s";

  /**
   * Temp blob name
   */
  private static final String TEST_BLOB_NAME = "temp123.txt";

  private static final String accountName = System.getenv(ACCOUNT_NAME);
  private static final String accountKey = System.getenv(ACCOUNT_KEY);
  private static final String containerName = System.getenv(CONTAINER_NAME);
  private static final String blobName = System.getenv(BLOB_NAME);

  public static void main(final String... strings) {

    //httpClientExample();

    try {
      blobCreateExample();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

  }

  /**
   * An example where a blob is created, downloaded and deleted for a given
   * account for a given container.
   *
   * @throws MalformedURLException
   */
  private static void blobCreateExample() throws MalformedURLException {

    /* Set the desired SAS signature values */
    final ServiceSASSignatureValues values = Util
        .getServiceSignatureVlaues(containerName, TEST_BLOB_NAME,
            TOKEN_EXPIRY_TIME);

    /* Set the desired permissions */
    final String permissions = Util
        .getPermissions(false, true, true, true, true, true);

    /* get the SAS Token */
    String sasURL = "";
    try {
      sasURL = getSASTokenForService(accountName, accountKey, containerName,
          TEST_BLOB_NAME, values, permissions);
    } catch (InvalidKeyException e) {
      System.out.println("Error acquiring SAS Token");
      e.printStackTrace();
      exit(-1);
    }

    final String blob = "Hello world!";

    final BlobURL blobURL = new BlobURL(new URL(sasURL), StorageURL
        .createPipeline(new AnonymousCredentials(), new PipelineOptions()));

    /* upload and wait until upload is done */
    blobURL.toBlockBlobURL()
        .upload(Flowable.just(ByteBuffer.wrap(blob.getBytes())), blob.length(),
            null, null, null, null).blockingGet();

    System.out.println(">>> Blob " + blob + " successfully uploaded.");

    final Single<DownloadResponse> downloadResponse = blobURL.download();
    final ByteBuffer buffer = FlowableUtil
        .collectBytesInBuffer(downloadResponse.blockingGet().body(null))
        .blockingGet();

    System.out.println(
        ">>> Downloaded blob is: " + StandardCharsets.UTF_8.decode(buffer)
            .toString());

    assert blob.equals(StandardCharsets.UTF_8.decode(buffer).toString());

    /* delete blob and wait */
    blobURL.delete().blockingGet();

    System.out.println(">>> Blob " + blob + " successfully deleted.");
  }

  /**
   * Example using http client
   */
  private static void httpClientExample() {

    /* Set the desired SAS signature values */
    final ServiceSASSignatureValues values = Util
        .getServiceSignatureVlaues(containerName, blobName, TOKEN_EXPIRY_TIME);
    /* Set the desired permissions */
    final String permissions = Util
        .getPermissions(false, true, false, false, true, false);
    String sasURL = "";
    try {
      sasURL = getSASTokenForService(accountName, accountKey, containerName,
          blobName, values, permissions);
    } catch (InvalidKeyException e) {
      System.out.println("Error acquiring SAS Token");
      e.printStackTrace();
    }

    /* get the blob */
    try {
      final String resultBlob = Request.Get(sasURL).connectTimeout(1000)
          .socketTimeout(1000).execute().returnContent().asString();

      System.out.println("Result:");
      System.out.println(resultBlob);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Function to get the SAS Token
   *
   * @return
   */
  private static String getSASTokenForService(final String accountName,
      final String accountKey, final String containerName,
      final String blobName, final ServiceSASSignatureValues values,
      final String permissions) throws InvalidKeyException {

    // create a credential object
    final SharedKeyCredentials credential = new SharedKeyCredentials(
        accountName, accountKey);

    values.withPermissions(permissions);

    // Generate SAS query params
    final SASQueryParameters params = values
        .generateSASQueryParameters(credential);

    final String encodeParams = params.encode();

    String sasUrl = String
        .format(Locale.ROOT, URL_FORMAT_BLOB, accountName, containerName,
            blobName, encodeParams);

    return sasUrl;

  }

}
