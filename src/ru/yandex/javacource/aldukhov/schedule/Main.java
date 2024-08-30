package ru.yandex.javacource.aldukhov.schedule;

import ru.yandex.javacource.aldukhov.schedule.manager.*;
import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        //Добавляем задачи
        Task task1 = new Task("Задача 1", "описание 1 задачи",
                Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task("Задача 2", "описание 2 задачи",
                Duration.ofHours(3), LocalDateTime.now().plusDays(1));
        manager.addNewTask(task1);
        manager.addNewTask(task2);

        Epic epic1 = new Epic("Эпик 1", "описание эпика 1");
        manager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1", "описание подзадачи 1", 3,
                Duration.ofHours(2), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "описание подзадачи 2", 3,
                Duration.ofHours(3), LocalDateTime.now().plusDays(2));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        Epic epic2 = new Epic("Эпик 2", "описание эпика 2");
        manager.addNewEpic(epic2);
        Subtask subtask3 = new Subtask("Подзадача 1", "описание подзадачи 1", 6,
                Duration.ofHours(3), LocalDateTime.now().plusDays(4));
        manager.addNewSubtask(subtask3);

        //Просматриваем задачи что бы вывести их в историю
        manager.taskById(1);
        manager.epicById(3);
        manager.subtaskById(4);

        manager.getHistory();

        //Просматриваем все задачи
        System.out.println("список задач: " + manager.getTasks());
        System.out.println("список эпиков: " + manager.getEpics());
        System.out.println("список подзадач: " + manager.getSubtask());
        System.out.println("\n");

        //Обновляем статус задач
        task1.setStatus(Status.DONE);
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.DONE);

        //Обновляем задачи с измененым статусом
        manager.updateTask(task1);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        manager.updateSubtask(subtask3);

        //Выводим список с обновленными задачами
        System.out.println("список задач c новым статусом: " + manager.getTasks());
        System.out.println("список эпиков c новым статусом: " + manager.getEpics());
        System.out.println("список подзадач c новым статусом: " + manager.getSubtask());
        System.out.println("\n");

        //удаляем задачи
        manager.delTaskById(task1.getId());
        manager.delEpicById(epic1.getId());

        //Выводим список с удаленными задачами
        System.out.println("Список задач после удаления: " + manager.getTasks());
        System.out.println("Список эпиков после удаления: " + manager.getEpics());
        System.out.println("Список подзадач после удаления: " + manager.getSubtask());

    }
}