package nukkitcoders.mobplugin.route;

import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import nukkitcoders.mobplugin.entities.WalkingEntity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zzz1999 @ MobPlugin
 */
public abstract class RouteFinder {
    private ArrayList<Node> nodes = new ArrayList<>();
    boolean finished = false;
    boolean searching = false;

    private int current = 0;

    public WalkingEntity entity;

    Vector3 start;
    Vector3 destination;

    protected Level level;

    private boolean interrupt = false;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    boolean reachable = true;

    RouteFinder(WalkingEntity entity) {
        Objects.requireNonNull(entity, "RouteFinder: entity can not be null");
        this.entity = entity;
        this.level = entity.getLevel();
    }

    public WalkingEntity getEntity() {
        return entity;
    }

    public Vector3 getStart() {
        return this.start;
    }

    public void setStart(Vector3 start) {
        if (!this.isSearching()) {
            this.start = start;
        }
    }

    public Vector3 getDestination() {
        return this.destination;
    }

    public void setDestination(Vector3 destination) {
        this.destination = destination;
        if (this.isSearching()) {
            this.interrupt = true;
            this.research();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isSearching() {
        return searching;
    }

    void addNode(Node node) {
        try {
            lock.writeLock().lock();
            nodes.add(node);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void addNode(ArrayList<Node> node) {
        try {
            lock.writeLock().lock();
            nodes.addAll(node);
        } finally {
            lock.writeLock().unlock();
        }

    }

    public boolean isReachable() {
        return reachable;
    }

    private Node getCurrentNode() {
        try {
            lock.readLock().lock();
            if (this.hasCurrentNode()) {
                return nodes.get(current);
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }

    }

    public boolean hasCurrentNode() {
        return current < this.nodes.size();
    }


    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public int getCurrent() {
        return this.current;
    }

    public boolean hasArrivedNode(Vector3 vec) {
        try {
            lock.readLock().lock();
            if (this.hasNext() && Objects.requireNonNull(this.getCurrentNode()).getVector3() != null) {
                Vector3 cur = this.getCurrentNode().getVector3();
                return vec.getX() == cur.getX() && vec.getZ() == cur.getZ();
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    void resetNodes() {
        try {
            this.lock.writeLock().lock();
            this.nodes.clear();
            this.current = 0;
            this.interrupt = false;
            this.destination = null;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public abstract void search();

    public void research() {
        this.resetNodes();
        this.search();
    }

    public boolean hasNext() {
        return this.current + 1 < nodes.size() && this.nodes.get(this.current + 1) != null;
    }

    public Vector3 next() {
        try {
            lock.readLock().lock();
            if (this.hasNext()) {
                return this.nodes.get(++current).getVector3();
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }

    }

    boolean isInterrupted() {
        return this.interrupt;
    }

    public boolean interrupt() {
        return this.interrupt ^= true;
    }
}
