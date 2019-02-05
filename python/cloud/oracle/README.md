# OCI examples

## Details

List OCI compute instances in particular region for given:

 - user
 - tenancy
 - region
 - compartment ID

Required function configuration:

  - OCI_USER - OCI user
  - OCI_TENANCY - OCI user tenancy
  - OCI_FINGERPRINT OCI user private key fingerprint
  - OCI_REGION - OCI region to talk to
  - OCI_COMPARTMENT - OCI compartment ID to search instances at
  - OCI_PRIVATE_KEY_BASE64 - OCI user private key encoded in BASE64
  - OCI_PRIVATE_KEY_PASS - OCI user private key pass phrase


Code available [here](oci-instances-func).

## Test code locally

Install `pytest`:

```bash
    python3 -m venv .venv
    source .venv/bin/activate
    pip3 install -r requirements.txt pytest
```

Run tests:
```bash
pytest -v -s --tb=long func.py
```

Result:
```bash
pytest -v -s --tb=long func.py 
=========================================================================================================================== test session starts ============================================================================================================================
platform darwin -- Python 3.7.1, pytest-4.0.1, py-1.7.0, pluggy-0.8.0 -- /Library/Frameworks/Python.framework/Versions/3.7/bin/python3.7
cachedir: .pytest_cache
rootdir: /xxxxxxxxxxxxx/go/src/github.com/fnproject/tutorials/python/cloud/oracle/oci-instances-func, inifile:
plugins: aiohttp-0.3.0
collected 1 item                                                                                                                                                                                                                                                           

func.py::test_list_instances 2019-01-22 18:57:10,964 - oci.base_client.4534029280 - INFO - Request: GET https://iaas.us-phoenix-1.oraclecloud.com/xxxxxxxxx/instances/
2019-01-22 18:57:10,972 - oci._vendor.urllib3.connectionpool - DEBUG - Starting new HTTPS connection (1): iaas.us-phoenix-1.oraclecloud.com
2019-01-22 18:57:11,990 - oci._vendor.urllib3.connectionpool - DEBUG - https://iaas.us-phoenix-1.oraclecloud.com:443 "GET /20160918/instances/?compartmentId=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx HTTP/1.1" 200 2
2019-01-22 18:57:11,991 - oci.base_client.4534029280 - DEBUG - Response status: 200
2019-01-22 18:57:11,991 - oci.base_client.4534029280 - DEBUG - python SDK time elapsed for deserializing: 0.0002571679999998633
2019-01-22 18:57:11,992 - oci.base_client.4534029280 - DEBUG - time elapsed for request: 1.027703139
PASSED

```

## Deployment

Deploying a function:

```bash
fn --verbose deploy --app oci
```

Update your local env with the following [script](oci-instances-func/setup_local.sh).

Updating configuration:
```bash
fn config fn oci list-instances OCI_USER ${OCI_USER}
fn config fn oci list-instances OCI_TENANCY ${OCI_TENANCY}
fn config fn oci list-instances OCI_FINGERPRINT ${OCI_FINGERPRINT}
fn config fn oci list-instances OCI_REGION ${OCI_REGION}
fn config fn oci list-instances OCI_COMPARTMENT ${OCI_COMPARTMENT}
fn config fn oci list-instances OCI_PRIVATE_KEY_BASE64 ${OCI_PRIVATE_KEY_BASE64}
fn config fn oci list-instances OCI_PRIVATE_KEY_PASS ${OCI_PRIVATE_KEY_PASS:-""}
```

Check the configuration:
```bash
fn inspect fn oci list-instances
```
make sure you're ok with a function's configuration you get from `inspect` command.
