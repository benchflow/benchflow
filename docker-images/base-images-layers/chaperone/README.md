# Base BenchFlow Image

This is a clone of: [https://github.com/garywiz/chaperone-docker/tree/master/alpinejava](https://github.com/garywiz/chaperone-docker/tree/master/alpinejava), documented on: [https://github.com/garywiz/chaperone-docker](https://github.com/garywiz/chaperone-docker). 

We cloned the original repository, because:

- It is rarely updated (last check on 19.04.2017). See also: [https://github.com/garywiz/chaperone-docker/issues/5](https://github.com/garywiz/chaperone-docker/issues/5)
- The original image set a user having restriction on creating folder in the root directory. Wercker needs such permission. Fore reference see: [https://github.com/wercker/wercker/issues/229](https://github.com/wercker/wercker/issues/229), [https://github.com/wercker/wercker/issues/262](https://github.com/wercker/wercker/issues/262). We now limit the permission of the user, only after we are done with Wercker duties. 