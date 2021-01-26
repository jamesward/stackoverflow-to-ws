stackoverflow-to-ws
-------------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Run Locally:

```
export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/creds.json
./sbt ~run
```

Run Locally with Containers:

```
export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/creds.json

pack build stackoverflow-to-ws

docker run -it \
  -v $GOOGLE_APPLICATION_CREDENTIALS:/home/user.json \
  -e GOOGLE_APPLICATION_CREDENTIALS=/home/user.json \
  -e APPLICATION_SECRET=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 32) \
  -e PORT=9000 -p 9000:9000 \
  stackoverflow-to-ws
```

Visit: [http://localhost:9000](http://localhost:9000)
