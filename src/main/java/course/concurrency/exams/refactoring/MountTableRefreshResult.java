package course.concurrency.exams.refactoring;

public class MountTableRefreshResult {

    private final boolean success;
    private final String adminAdress;

    public MountTableRefreshResult(boolean success, String adminAdress) {
        this.success = success;
        this.adminAdress = adminAdress;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getAdminAdress() {
        return adminAdress;
    }
}
