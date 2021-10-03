package su.grinev;

import java.util.Objects;

public class MatrixElement {

    private Character character;
    private Integer colorIndex;

    public MatrixElement(Character character) {
        this.character = character;
        this.colorIndex = 0;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public Integer getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(Integer colorIndex) {
        this.colorIndex = colorIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatrixElement that = (MatrixElement) o;
        return Objects.equals(character, that.character) && Objects.equals(colorIndex, that.colorIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, colorIndex);
    }

    @Override
    public String toString() {
        return "MatrixElement{" +
                "character=" + character +
                ", color=" + colorIndex +
                '}';
    }
}
