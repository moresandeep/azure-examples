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
import com.microsoft.azure.storage.blob.BlobSASPermission;
import com.microsoft.azure.storage.blob.BlobURL;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerSASPermission;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.SASProtocol;
import com.microsoft.azure.storage.blob.SASQueryParameters;
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.apache.http.client.fluent.Request;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;

import static java.lang.System.exit;

public class SASTokenPOC {

  public static final String ACCOUNT_NAME = "AZURE_STORAGE_ACCOUNT";
  public static final String ACCOUNT_KEY = "AZURE_STORAGE_ACCESS_KEY";

  public static final String CONTAINER_NAME = "STORAGE_CONTAINER_NAME";
  public static final String BLOB_NAME = "STORAGE_BLOB_NAME";

  private static final OffsetDateTime TOKEN_EXPIARY_TIME = OffsetDateTime.now().plusDays(1);

  private static final String URL_FORMAT_ACCOUNT = "https://%s.blob.core.windows.net/%s/%s%s";
  private static final String URL_FORMAT_BLOB = "https://%s.blob.core.windows.net/%s/%s%s";

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
      tempFile = createTempFile();
    } catch (IOException e) {
      System.out.println("Error creating temp file");
      e.printStackTrace();
    }

    //httpClientExample();

    try {
      blobExample();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

  }

  /**
   * This example downloads a blob {@link #BLOB_NAME}
   * under container {@link #CONTAINER_NAME} defined by
   * environment vriables.
   *
   * @throws MalformedURLException
   */
  private static void blobExample() throws MalformedURLException {

    /* Set the desired SAS signature values */
    final ServiceSASSignatureValues values = getSignatureVlaues(containerName, blobName, TOKEN_EXPIARY_TIME);

    /* Set the desired permissions */
    final String permissions = getPermissions(false, true, true,true,true,true);

    /* get the SAS Token */
    String sasURL = "";
    try {
      sasURL = getSASToken(accountName, accountKey, containerName, blobName, values, permissions);
    } catch (InvalidKeyException e) {
      System.out.println("Error acquiring SAS Token");
      e.printStackTrace();
      exit(-1);
    }

    System.out.println("SAS Token URL: "+sasURL);

    /* NOTE: we do not use account credentials here */
    final HttpPipeline pipeline = StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions());

    /* create service url object */
    final ServiceURL serviceURL = new ServiceURL(new URL(sasURL), pipeline);

    //ContainerURL containerURL = serviceURL.createContainerURL(TEST_CONTAINER_NAME);

    final ContainerURL containerURL =  new ContainerURL(new URL(sasURL), StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

    final BlobURL blobURL = new BlobURL(new URL(sasURL), StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

    Single<DownloadResponse> response =  blobURL.download();

    ByteBuffer buffer = FlowableUtil.collectBytesInBuffer(response.blockingGet().body(null)).blockingGet();

    System.out.println("Body is: "+StandardCharsets.UTF_8.decode(buffer).toString());

    /* Blob to be created */
    //final BlockBlobURL blobURL = new BlockBlobURL(new URL(sasURL), StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

    //String blob = "Hello world!";


//    /* async api */
//    containerURL.create()
//        /* upload */
//        .flatMap(containersCreateResponse ->
//            /* upload the string as blob */
//            blobURL.upload(Flowable.just(ByteBuffer.wrap(blob.getBytes())), blob.length()) )
//        /* download */
//        .flatMap( downloadReponse ->
//            /* download the blob */
//            blobURL.download())
//        /* test */
//        .flatMap( downloadReponse ->
//            FlowableUtil.collectBytesInBuffer(downloadReponse.body(null))
//                .doOnSuccess( buffer -> {
//                  /* compare upload and download, throw error if they don't match up */
//                  if(buffer.compareTo(ByteBuffer.wrap(blob.getBytes())) != 0) {
//                    throw new Exception("Uploaded and downloaded data don't match, expected: "+blob+ " got: "+ StandardCharsets.UTF_8.decode(buffer).toString());
//                  } else {
//                    System.out.println("Uploaded data matches downloaded data");
//                  }
//                }))
//        /* delete blob */
//        .flatMap(buffer ->
//            blobURL.delete())
//        /* delete container */
//        .flatMap(deleteResponse ->
//            containerURL.delete())
//        /* I want results, NOW ! */
//        .blockingGet();
  }

  /**
   * Example using http client
   */
  private static void httpClientExample() {

    /* Set the desired SAS signature values */
    final ServiceSASSignatureValues values = getSignatureVlaues(containerName, blobName, TOKEN_EXPIARY_TIME);
    /* Set the desired permissions */
    final String permissions = getPermissions(false, true, false,false,true,false);
    String sasURL = "";
    try {
      sasURL = getSASToken(accountName, accountKey, containerName, blobName, values, permissions);
    } catch (InvalidKeyException e) {
      System.out.println("Error acquiring SAS Token");
      e.printStackTrace();
    }

    System.out.println(sasURL);

    /* get the blob */
    try {
      final String resultBlob = Request.Get(sasURL)
          .connectTimeout(1000)
          .socketTimeout(1000)
          .execute().returnContent().asString();

      System.out.println("Result:");
      System.out.println(resultBlob);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Function that sets the desired signature values
   * @return
   */
  private static ServiceSASSignatureValues getSignatureVlaues(final String containerName, final String blobName, final OffsetDateTime expiryTime) {

    if(blobName == null) {
      return new ServiceSASSignatureValues()
          .withProtocol(SASProtocol.HTTPS_ONLY)
          .withExpiryTime(expiryTime)
          .withContainerName(containerName);
    } else {
      return new ServiceSASSignatureValues()
          .withProtocol(SASProtocol.HTTPS_ONLY)
          .withExpiryTime(expiryTime)
          .withContainerName(containerName)
          .withBlobName(blobName);
    }

  }

  private static String getPermissions(final boolean isContainer,
      final boolean read, final boolean write,
      final boolean add, final boolean list, final boolean delete) {

    if (isContainer) {
      /* permissions for container */
      final ContainerSASPermission permission = new ContainerSASPermission()
          .withRead(read)
          .withList(list)
          .withAdd(add)
          .withWrite(write)
          .withDelete(delete);
      return permission.toString();
    } else {
      /* permissions for blob */
      final BlobSASPermission permission = new BlobSASPermission()
          .withRead(read)
          .withAdd(add)
          .withWrite(write)
          .withDelete(delete);
      return permission.toString();
    }
  }

  /**
   * Function to get the SAS Token
   * @return
   */
  private static String getSASToken(final String accountName, final String accountKey, final String containerName, final String blobName, final ServiceSASSignatureValues values, final String permissions) throws InvalidKeyException {

    // create a credential object
    final SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

    values.withPermissions(permissions);

    // Generate SAS query params
    final SASQueryParameters params = values.generateSASQueryParameters(credential);

    final String encodeParams = params.encode();
    String sasUrl = null;

    if(blobName == null) {
      sasUrl = String.format(Locale.ROOT, URL_FORMAT_ACCOUNT, accountName, containerName, encodeParams);
    } else {
      sasUrl = String.format(Locale.ROOT, URL_FORMAT_BLOB, accountName, containerName, blobName, encodeParams);
    }

    return sasUrl;

  }

  /**
   * Function to create a temp file for testing.
   * @return
   */
  private static File createTempFile() throws IOException {
    File tempFile = File.createTempFile("temp", ".txt");
    System.out.println(" >> Temp file created " + tempFile.toString());
    Writer output = new BufferedWriter(new FileWriter(tempFile));
    output.write("Hello World !");
    output.close();
    return tempFile;
  }

}
