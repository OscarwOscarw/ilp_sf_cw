package data;

public class RestrictedAreaBlockageException extends RuntimeException {
    private final Integer taskId;
    private final String restrictedAreaName;
    private final boolean requiresHumanConfirmation;

    public RestrictedAreaBlockageException(Integer taskId, String restrictedAreaName, boolean requiresHumanConfirmation) {
        super(buildMessage(taskId, restrictedAreaName, requiresHumanConfirmation));
        this.taskId = taskId;
        this.restrictedAreaName = restrictedAreaName;
        this.requiresHumanConfirmation = requiresHumanConfirmation;
    }

    private static String buildMessage(Integer taskId, String restrictedAreaName, boolean requiresHumanConfirmation) {
        if (requiresHumanConfirmation) {
            return taskId + " is restricted by" + restrictedAreaName + "Fly or not?";
        } else {
            return taskId + " is restricted by" + restrictedAreaName;
        }
    }

    // Getters
    public Integer getTaskId() { return taskId; }
    public String getRestrictedAreaName() { return restrictedAreaName; }
    public boolean isRequiresHumanConfirmation() { return requiresHumanConfirmation; }
}