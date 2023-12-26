package course.concurrency.exams.refactoring;

public class MountTableRefresherTaskFactory {
    public MountTableRefresherTask create(Others.MountTableManager manager, String address) {
        return new MountTableRefresherTask(manager, address);
    }
}
