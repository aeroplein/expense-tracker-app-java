package model;

import java.awt.*;

public class Category {

    private int id;
    private String name;
    private String colorHex;
    private int userId;

    public Category(int id, String name, String colorHex, int userId){
        this.id=id;
        this.name=name;
        this.colorHex=colorHex;
        this.userId=userId;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public Color toColor(){
        try{
            return Color.decode(colorHex);
        }catch(NumberFormatException e){
            return Color.GRAY;
        }
    }

    @Override
    public String toString(){
        return name;
    }

}
