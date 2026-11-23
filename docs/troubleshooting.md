# Troubleshooting

## Why does `getMe()` return HTTP 401?

Check that you passed a valid personal API token to the client. If you
followed the quick start, check that `CIRCLECI_TOKEN` is set in the process
running your application. For CircleCI Server on master, pass a token
issued by the same instance you pass to `server()` or `serverAsync()`.

## Why does client creation fail with "CircleCI API token contains invalid header characters"?

On master, the v2 and v3 clients validate the token when the client is
created. If you read the token from a file or copied it from a command's
output, remove any surrounding line breaks before passing it to the client.

## Why does the Server client reject my instance URL?

Pass the HTTPS origin, such as `https://ci.example.com:8443`. The client
adds the API path itself. Remove any path such as `/api/v2/`, query string,
fragment, or embedded credentials. An optional trailing slash is accepted.
If the instance redirects to a different HTTPS origin, use its canonical
HTTPS origin directly.

## Why do the Observable imports differ between the release and master?

Version `2.1.2` uses RxJava 2 (`io.reactivex.Observable`). Builds from
master use RxJava 3 (`io.reactivex.rxjava3.core.Observable`). Match the import
and any reactive integrations to the library version your application uses.
