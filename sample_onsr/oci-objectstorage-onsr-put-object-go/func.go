/*
 oci-objectstorage-onsr-put-object-go version 1.0.

 Copyright (c) 2021 Oracle, Inc.  All rights reserved.
 Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
*/

package main

import (
	"context"
	//"crypto/tls"
	//"crypto/x509"
	"encoding/json"
	"fmt"
	"io"
	//"io/ioutil"
	"os"
	"path"

	fdk "github.com/fnproject/fdk-go"
	"github.com/oracle/oci-go-sdk/v37/common"
	"github.com/oracle/oci-go-sdk/v37/common/auth"
	"github.com/oracle/oci-go-sdk/v37/example/helpers"
	"github.com/oracle/oci-go-sdk/v37/objectstorage"
)

type ObjectStorage_Bucket struct {
	Name string `json:"bucket"`
}

func main() {
	fdk.Handle(fdk.HandlerFunc(myHandler))
}

func putObject(ctx context.Context, c objectstorage.ObjectStorageClient, namespace, bucketname, objectname string, contentLen int64, content io.ReadCloser, metadata map[string]string) error {
	request := objectstorage.PutObjectRequest{
		NamespaceName: common.String(namespace),
		BucketName:    common.String(bucketname),
		ObjectName:    common.String(objectname),
		ContentLength: common.Int64(contentLen),
		PutObjectBody: content,
		OpcMeta:       metadata,
	}
	_, err := c.PutObject(ctx, request)
	fmt.Println("put object")
	return err
}

func ObjectStorage_UploadFile(ctx context.Context, bname string) {
	// Get auth
	provider, err := auth.ResourcePrincipalConfigurationProvider()
	helpers.FatalIfError(err)

	client, client_err := objectstorage.NewObjectStorageClientWithConfigurationProvider(provider)
	helpers.FatalIfError(client_err)

	/*
		// Certs are mounted at this location for ONSR realms
		cert_file_path := "/python/certifi/cacert.pem"
		cert, err := ioutil.ReadFile(cert_file_path)
		helpers.FatalIfError(err)

		// Adding extra certs
		pool := x509.NewCertPool()
		pool.AppendCertsFromPEM([]byte(string(cert)))

		//install the certificates to the client
		if h, ok := client.HTTPClient.(*http.Client); ok {
			tr := &http.Transport{TLSClientConfig: &tls.Config{RootCAs: pool}}
			h.Transport = tr
		} else {
			panic("the client dispatcher is not of http.Client type. can not patch the tls config")
		}
	*/

	request := objectstorage.GetNamespaceRequest{}
	r, err := client.GetNamespace(ctx, request)
	helpers.FatalIfError(err)

	namespace := *r.Value

	contentlen := 1024 * 1000
	filepath, filesize := helpers.WriteTempFileOfSize(int64(contentlen))
	filename := path.Base(filepath)

	file, e := os.Open(filepath)
	defer file.Close()
	helpers.FatalIfError(e)

	e = putObject(ctx, client, namespace, bname, filename, filesize, file, nil)
	helpers.FatalIfError(e)
}

func myHandler(ctx context.Context, in io.Reader, out io.Writer) {
	bucket := &ObjectStorage_Bucket{Name: "bucket"}
	json.NewDecoder(in).Decode(bucket)
	ObjectStorage_UploadFile(ctx, bucket.Name)
	msg := struct {
		Msg string `json:"message"`
	}{
		Msg: "Request Completed",
	}
	json.NewEncoder(out).Encode(&msg)
}
