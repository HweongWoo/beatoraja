package bms.player.beatoraja.select;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.skin.*;
import bms.player.beatoraja.skin.Skin.SkinObjectRenderer;

import com.badlogic.gdx.graphics.g2d.*;

/**
 * 璵썸쎊�깘�꺖�룒�뵽�뵪�궧�궘�꺍�궕�깣�궦�궒�궚�깉
 */
public class SkinBar extends SkinObject {

    /**
     * �겦�뒢�셽�겗Bar�겗SkinImage
     */
    private SkinImage[] barimageon = new SkinImage[60];
    /**
     * �씆�겦�뒢�셽�겗Bar�겗SkinImage
     */
    private SkinImage[] barimageoff = new SkinImage[60];
    /**
     * �깉�꺆�깢�궍�꺖�겗SkinImage�귝룒�뵽鵝띸쉰�겘Bar�겗�쎑野얍벨與�
     */
    private SkinImage[] trophy = new SkinImage[3];

    private SkinText[] text = new SkinText[2];
    /**
     * �꺃�깧�꺂�겗SkinNumber�귝룒�뵽鵝띸쉰�겘Bar�겗�쎑野얍벨與�
     */
    private SkinNumber[] barlevel = new SkinNumber[7];
    /**
     * 鈺쒒씊�꺀�깧�꺂�겗SkinImage�귝룒�뵽鵝띸쉰�겘Bar�겗�쎑野얍벨與�
     */
    private SkinImage[] label = new SkinImage[3];

    private SkinDistributionGraph graph;

    private int position = 0;

    private TextureRegion[][] images;
    private int cycle;

    /**
     * �꺀�꺍�깤�뵽�깗
     */
    private SkinImage[] lamp = new SkinImage[11];
    /**
     * �꺀�궎�깘�꺂�꺀�꺍�깤烏①ㅊ�셽�겗�깤�꺃�궎�깶�꺖�꺀�꺍�깤�뵽�깗
     */
    private SkinImage[] mylamp = new SkinImage[11];
    /**
     * �꺀�궎�깘�꺂�꺀�꺍�깤烏①ㅊ�셽�겗�꺀�궎�깘�꺂�꺀�꺍�깤�뵽�깗
     */
    private SkinImage[] rivallamp = new SkinImage[11];

    public SkinBar(int position) {
        this.position = position;
        SkinDestinationSize dstSize = new SkinDestinationSize(0, 0, 0, 0);
        this.setDestination(0, dstSize, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, new int[0]);
    }

    public SkinBar(int position, TextureRegion[][] images, int cycle) {
        setBarImages(images, cycle);
    }

    public void setBarImages(TextureRegion[][] images, int cycle) {
        this.images = images;
        this.cycle = cycle;
    }
    
    public void setBarImage(SkinImage[] onimage, SkinImage[] offimage) {
    	barimageon = onimage;
    	barimageoff = offimage;
    }

    public SkinImage makeBarImages(boolean on, int index) {
        if ((on ? barimageon[index] : barimageoff[index]) == null) {
            if (on) {
                barimageon[index] = new SkinImage(images, 0, cycle);
            } else {
                barimageoff[index] = new SkinImage(images, 0, cycle);
            }
        }
        return on ? barimageon[index] : barimageoff[index];
    }

    public SkinImage getBarImages(boolean on, int index) {
    	if(index >= 0 && index < barimageoff.length) {
            return on ? barimageon[index] : barimageoff[index];    		
    	}
    	return null;
    }

    public SkinImage[] getLamp() {
        return lamp;
    }

    public SkinImage[] getPlayerLamp() {
        return mylamp;
    }

    public SkinImage[] getRivalLamp() {
        return rivallamp;
    }

    public SkinImage[] getTrophy() {
        return trophy;
    }

    public SkinText[] getText() {
        return text;
    }

    public void setTrophy(SkinImage[] trophy) {
        this.trophy = trophy;
    }

    public void setLamp(SkinImage[] lamp) {
        this.lamp = lamp;
    }

    public void setPlayerLamp(SkinImage[] lamp) {
        this.mylamp = lamp;
    }

    public void setRivalLamp(SkinImage[] lamp) {
        this.rivallamp = lamp;
    }

    @Override
    public void draw(SkinObjectRenderer sprite, long time, MainState state) {
        ((MusicSelector)state).getBarRender().render(sprite, (MusicSelectSkin) state.getSkin(), this, (int)time);
    }

    @Override
    public void dispose() {
    	disposeAll(barimageon);
    	disposeAll(barimageoff);
    	disposeAll(trophy);
    	disposeAll(text);
    	disposeAll(barlevel);
    	disposeAll(label);
        disposeAll(lamp);
        disposeAll(mylamp);
        disposeAll(rivallamp);
    }

    public SkinNumber[] getBarlevel() {
        return barlevel;
    }

    public void setBarlevel(SkinNumber[] barlevel) {
        this.barlevel = barlevel;
    }

    public int getPosition() {
        return position;
    }
    
    @Override
	protected boolean mousePressed(MainState state, int button, int x, int y) {
        return ((MusicSelector) state).getBarRender().mousePressed(this, button, x, y);
	}

    public SkinImage[] getLabel() {
        return label;
    }

    public void setLabel(SkinImage[] label) {
        this.label = label;
    }

	public SkinDistributionGraph getGraph() {
		return graph;
	}

	public void setGraph(SkinDistributionGraph graph) {
		this.graph = graph;
	}
}