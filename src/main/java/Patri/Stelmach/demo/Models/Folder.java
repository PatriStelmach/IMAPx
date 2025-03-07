package Patri.Stelmach.demo.Models;

public enum Folder
{
    spam("spam"), inbox("inbox"), sent("sent"), oldRed("old-red");
    private String name;
    Folder(String name)
    {
        this.name = name;
    }
}
