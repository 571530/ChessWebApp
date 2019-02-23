package no.hvl.chessapp;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@org.springframework.stereotype.Controller
public class Controller {

    @GetMapping("/newgame")
    public String newGame() {
        return "newgame";
    }

    @PostMapping("/newgame")
    public String createNewGame(HttpSession session, @RequestParam String difficulty) {
        session.setAttribute("game", new ChessGame(difficulty));

        return "redirect:/";
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        if (session.getAttribute("game") == null) {
            return "redirect:/newgame";
        }
        ChessGame game = (ChessGame) session.getAttribute("game");

        model.addAttribute("fen", game.getFEN());

        return "index";
    }

    @GetMapping("/play")
    public @ResponseBody String makeComputerMove(HttpSession session, @RequestParam String move) {
        if (session.getAttribute("game") == null) {
            return "no game found";
        }

        ChessGame game = (ChessGame) session.getAttribute("game");

        game.makePlayerMove(move);

        return game.makeComputerMove().getSAN();
    }

}
