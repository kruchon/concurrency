package course.concurrency.exams.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class MountTableRefresherService {

    private Others.RouterStore routerStore = new Others.RouterStore();
    private long cacheUpdateTimeout;

    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;

    private MountTableRefresherTaskFactory taskFactory;

    public void serviceInit() {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new Others.LoadingCache<String, Others.RouterClient>();
        routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));
        taskFactory = new MountTableRefresherTaskFactory();
        initClientCacheCleaner(routerClientMaxLiveTime);
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setName("MountTableRefresh_ClientsCacheCleaner");
                t.setDaemon(true);
                return t;
            }
        };

        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and
         * closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh() {

        List<Others.RouterState> cachedRecords = routerStore.getCachedRecords();
        List<MountTableRefresherTask> refreshTasks = new ArrayList<>();
        for (Others.RouterState routerState : cachedRecords) {
            String adminAddress = routerState.getAdminAddress();
            if (adminAddress == null || adminAddress.length() == 0) {
                // this router has not enabled router admin.
                continue;
            }
            if (isLocalAdmin(adminAddress)) {
                /*
                 * Local router's cache update does not require RPC call, so no need for
                 * RouterClient
                 */
                refreshTasks.add(getLocalRefresher(adminAddress));
            } else {
                refreshTasks.add(taskFactory.create(
                        new Others.MountTableManager(adminAddress), adminAddress));
            }
        }
        if (!refreshTasks.isEmpty()) {
            invokeRefresh(refreshTasks);
        }
    }

    protected MountTableRefresherTask getLocalRefresher(String adminAddress) {
        return taskFactory.create(new Others.MountTableManager("local"), adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private void invokeRefresh(List<MountTableRefresherTask> refreshTasks) {
        /*
         * Wait for all the thread to complete, await method returns false if
         * refresh is not finished within specified time
         */
        List<MountTableRefreshResult> results = refreshTasks
                .stream()
                .map(CompletableFuture::supplyAsync)
                .map(t -> t
                    .exceptionally(e -> null)
                    .completeOnTimeout(null, cacheUpdateTimeout, TimeUnit.MILLISECONDS)
                    .join()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (refreshTasks.size() != results.size()) {
            log("Not all router admins updated their cache");
        }
        logResult(results);
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contains("local");
    }

    private void logResult(List<MountTableRefreshResult> refreshThreads) {
        int successCount = 0;
        int failureCount = 0;
        for (MountTableRefreshResult mountTableRefreshThread : refreshThreads) {
            if (mountTableRefreshThread.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
                // remove RouterClient from cache so that new client is created
                removeFromCache(mountTableRefreshThread.getAdminAdress());
            }
        }
        log(String.format(
                "Mount table entries cache refresh successCount=%d,failureCount=%d",
                successCount, failureCount));
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
        this.cacheUpdateTimeout = cacheUpdateTimeout;
    }

    public void setRouterClientsCache(Others.LoadingCache cache) {
        this.routerClientsCache = cache;
    }

    public void setRouterStore(Others.RouterStore routerStore) {
        this.routerStore = routerStore;
    }

    public void setTaskFactory(MountTableRefresherTaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }
}