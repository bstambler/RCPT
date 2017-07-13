package com.RobustSystems;

public interface ITaskListener {
	void onTaskStart();
	void onTaskCompleted();
	void onTaskProgress(int size);
}
