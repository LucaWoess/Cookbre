import java.awt.Image;

public class Dish {
	private int dishID;
	private String dishName;
	private String dishCookingInstruction;
	private Image dishImage;
	private boolean isVeggie;
	
	public int getDishID() {
		return dishID;
	}
	public void setDishID(int dishID) {
		this.dishID = dishID;
	}
	public String getDishName() {
		return dishName;
	}
	public void setDishName(String dishName) {
		this.dishName = dishName;
	}
	public String getDishCookingInstruction() {
		return dishCookingInstruction;
	}
	public void setDishCookingInstruction(String dishCookingInstruction) {
		this.dishCookingInstruction = dishCookingInstruction;
	}
	public Image getDishImage() {
		return dishImage;
	}
	public void setDishImage(Image dishImage) {
		this.dishImage = dishImage;
	}
	public boolean getIsVeggie() {
		return isVeggie;
	}
	public void setIsVeggie(boolean isVeggie) {
		this.isVeggie = isVeggie;
	}
	
}
