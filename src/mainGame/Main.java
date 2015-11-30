package mainGame;

import Network.GameClient;
import org.newdawn.slick.*;
import org.newdawn.slick.state.StateBasedGame;

public class Main extends StateBasedGame
{

    public static final int ScreenWidth = 1280;
    public static final int ScreenHeight = 720;

    public Main(String gameName)
    {
        super(gameName);
    }

    public static void main(String[] args) throws SlickException
    {
        AppGameContainer appgc = new AppGameContainer(new Main("Settlers of Catan"));

        appgc.setDisplayMode(ScreenWidth, ScreenHeight, false);
        appgc.setAlwaysRender(true);

        (new Thread(new GameClient())).start();

        appgc.start();
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException
    {
        this.addState(new MainMenuState());
        this.addState(new CreatePlayerState());
        this.addState(new JoinLobbyState());
        this.addState(new PlayingWindow());
    }
}