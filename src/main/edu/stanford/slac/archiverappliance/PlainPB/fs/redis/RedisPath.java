package edu.stanford.slac.archiverappliance.PlainPB.fs.redis;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * When storing data in Redis, we use the chunkKey as the redis key name.
 * This puts all the data in the "root" folder, so some of the path manipulations here may not make a lot of sense. 
 * @author mshankar
 *
 */
public class RedisPath implements Path {
	private static final Logger logger = Logger.getLogger(RedisPath.class.getName());
	
	/**
	 * The file system provider for redis.
	 */
	private RedisFileSystemProvider redisFSProvider;

	/**
	 * This is the connection name; a string used to identify the Jedis object being used in the FileSystemProvider's list of FileSystem's.
	 */
	private String connectionName;
	
	/**
	 * This is the redis key; so by the time we initialize the path, we should have stripped the scheme and server
	 */
	private String redisKey;
	private Path key;	
	
	private boolean isAbsolute = false;
	
	/**
	 * @param connectionName - This is the string used to identify the redis connection for connecting to this path. 
	 * @param pathSuffix - This is the key (pathName). 
	 * This gets appended to the path portion of the URI that was used to create the file system.
	 * 
	 */
	public RedisPath(RedisFileSystemProvider redisFSProvider, String connectionName, String pathSuffix) {
		this(redisFSProvider, connectionName, pathSuffix, false);
	}
	
	public RedisPath(RedisFileSystemProvider redisFSProvider, String connectionName, String pathSuffix, boolean isAbsolute) {
		this.redisFSProvider = redisFSProvider;
		this.connectionName = connectionName;
		assert(!pathSuffix.contains("redis:"));
		this.redisKey = pathSuffix;
		this.key = Paths.get(this.redisKey);
		this.isAbsolute = isAbsolute;
	}
	
	@Override
	public FileSystem getFileSystem() {
		return redisFSProvider.getFileSystem(connectionName);
	}

	@Override
	public boolean isAbsolute() {
		return isAbsolute;
	}

	@Override
	public Path getRoot() {
		return new RedisPath(redisFSProvider, connectionName, "/");
	}

	@Override
	public Path getFileName() {
		return key.getFileName();
	}

	@Override
	public Path getParent() {
		return new RedisPath(redisFSProvider, connectionName, key.getParent().toString());
	}

	@Override
	public int getNameCount() {
		return key.getNameCount();
	}

	@Override
	public URI toUri() {
		try {
			return new URI("redis://" + this.connectionName + "/" + this.key.toString());
		} catch (URISyntaxException e) {
			logger.error("Exception generating URI", e);
			return null;
		}
	}

	@Override
	public File toFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Path toAbsolutePath() {
		return new RedisPath(redisFSProvider, connectionName, key.toAbsolutePath().toString(), true);
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path getName(int index) {
		return key.getName(index);
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		return key.subpath(beginIndex, endIndex);
	}

	@Override
	public boolean startsWith(Path other) {
		return key.startsWith(other);
	}

	@Override
	public boolean startsWith(String other) {
		return key.startsWith(other);
	}

	@Override
	public boolean endsWith(Path other) {
		return key.endsWith(other);
	}

	@Override
	public boolean endsWith(String other) {
		return key.endsWith(other);
	}

	@Override
	public Path normalize() {
		return new RedisPath(redisFSProvider, connectionName, key.normalize().toString());
	}

	@Override
	public Path resolve(Path other) {
		return new RedisPath(redisFSProvider, connectionName, key.resolve(other).toString());
	}

	@Override
	public Path resolve(String other) {
		return new RedisPath(redisFSProvider, connectionName, key.resolve(other).toString());
	}

	@Override
	public Path resolveSibling(Path other) {
		return new RedisPath(redisFSProvider, connectionName, key.resolveSibling(other).toString());
	}

	@Override
	public Path resolveSibling(String other) {
		return new RedisPath(redisFSProvider, connectionName, key.resolveSibling(other).toString());
	}

	@Override
	public Path relativize(Path other) {
		return new RedisPath(redisFSProvider, connectionName, key.relativize(other).toString());
	}

	@Override
	public int compareTo(Path other) {
		if(other == null) { 
			return 1;
		}
		
		if(other instanceof RedisPath) {
			return key.compareTo(((RedisPath)other).key);			
		} else { 
			return 1;
		}
	}

	@Override
	public Iterator<Path> iterator() {
		return key.iterator();
	}

	public String getRedisKey() {
		return redisKey;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public Path getKey() {
		return key;
	}

	@Override
	public String toString() {
		if(this.isAbsolute) {
			return "redis://" + this.connectionName + this.redisKey;
		} else { 
			return this.redisKey;
		}
	}
}
