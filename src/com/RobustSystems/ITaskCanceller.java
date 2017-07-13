package com.RobustSystems;

public interface ITaskCanceller {
	boolean isTaskCancelled();
	void onTaskProgress(int progress);
}