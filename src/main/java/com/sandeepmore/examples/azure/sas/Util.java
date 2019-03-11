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

import com.microsoft.azure.storage.blob.AccountSASSignatureValues;
import com.microsoft.azure.storage.blob.BlobSASPermission;
import com.microsoft.azure.storage.blob.ContainerSASPermission;
import com.microsoft.azure.storage.blob.SASProtocol;
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;

public class Util {

  /**
   * Function to create a temp file for testing.
   * @return
   */
  public static File createTempFile() throws IOException {
    File tempFile = File.createTempFile("temp", ".txt");
    System.out.println(" >> Temp file created " + tempFile.toString());
    Writer output = new BufferedWriter(new FileWriter(tempFile));
    output.write("Hello World !");
    output.close();
    return tempFile;
  }

  /**
   * Function to get the SAS Signature for Account SAS access.
   * @param expiryTime
   * @return
   */
  public static AccountSASSignatureValues getAccountSignatureValues(final OffsetDateTime expiryTime) {
    return new AccountSASSignatureValues()
        .withProtocol(SASProtocol.HTTPS_ONLY)
        .withExpiryTime(expiryTime);
  }

  /**
   * Function that sets the desired signature values for Service SAS access.
   * @return
   */
  public static ServiceSASSignatureValues getServiceSignatureVlaues(final String containerName, final String blobName, final OffsetDateTime expiryTime) {
    return new ServiceSASSignatureValues()
        .withProtocol(SASProtocol.HTTPS_ONLY)
        .withExpiryTime(expiryTime)
        .withContainerName(containerName)
        .withBlobName(blobName);
  }

  public static String getPermissions(final boolean isContainer,
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



}
