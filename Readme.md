## How to set up this project

1. Go to https://opencv.org/releases/, and download the release of OpenCV of version *4.7.0*
2. Unzip opencv to anywhere you want
3. Add \opencv\build\java\64 to your environment variables. If you are using Mac, then check out https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#install-opencv-3-x-under-windows
3. Navigate to \opencv\build\java, copy the file path of opencv-470.jar
4. Open pom.xml located in the root of this project, and replace opencv.path property with the jar file path.
5. Refresh maven.
6. Make sure you also have `vm.max_map_count=262144` added to `/etc/sysctl.conf` if you are running docker in WSL.
7. Run `docker-compose up`.