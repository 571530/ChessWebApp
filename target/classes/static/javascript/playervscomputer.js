let fen = document.getElementById("fen").value;

let board,
    game = new Chess(fen);

let your_turn = true;
let game_over = false;

const CHECK_SYZYGY = false;

let removeGreySquares = function () {
    $('#board .square-55d63').css('background', '');
};

let afterComputerMove = function () {
    your_turn = true;
}

let makeComputerMove = function (playermove) {
    your_turn = false;

    // If the position has less than 7 pieces left the position is in the sygyzy database
    if (Object.keys(board.position()).length <= 7 && CHECK_SYZYGY) {
        console.log("Checking lichess database");
        // Checking syzygy database using lichess api
        $.get("http://tablebase.lichess.ovh/standard", {
                fen: game.fen()
            },
            function (data, status) {
                if (data.moves.length > 0 && data.moves[0].dtz != null && data.moves[0].dtz != 0) {
                    console.log(data.moves[0].san);
                    game.move(data.moves[0].san);
                    board.position(game.fen());
                    setTimeout(afterComputerMove, WAIT_TIME);
                } else {
                    return computerMove(playermove, afterComputerMove);
                }
            });
    } else {
        return computerMove(playermove, afterComputerMove);
    }
}

let computerMove = function (playermove, then) {
    your_turn = false;

    $.get("play",
        {
            move: playermove
        }, function (move, status) {
            if (move.includes("end")) {
                console.log(move);
                game_over = true;
            } else {
                console.log(move);

                game.move(move);
                board.position(game.fen());
                your_turn = true;

                setTimeout(then, 250);
            }

        });
}

let greySquare = function (square) {
    let squareEl = $('#board .square-' + square);

    let background = '#a9a9a9';
    if (squareEl.hasClass('black-3c85d') === true) {
        background = '#696969';
    }

    squareEl.css('background', background);
};

let onDragStart = function (source, piece) {
    if (!your_turn) return false;
    // do not pick up pieces if the game is over
    // or if it's not that side's turn
    if (game.game_over() === true ||
        (game.turn() === 'w' && piece.search(/^b/) !== -1) ||
        (game.turn() === 'b' && piece.search(/^w/) !== -1)) {
        return false;
    }
};

let onDrop = function (source, target) {
    removeGreySquares();

    // see if the move is legal
    let move = game.move({
        from: source,
        to: target,
        promotion: 'q' // NOTE: always promote to a queen for example simplicity
    });

    // illegal move
    if (move === null) return 'snapback';

    setTimeout(makeComputerMove.bind(null, move.san), 200);
};

let onMouseoverSquare = function (square, piece) {
    // get list of possible moves for this square
    let moves = game.moves({
        square: square,
        verbose: true
    });

    // exit if there are no moves available for this square
    if (moves.length === 0) return;

    // highlight the square they moused over
    greySquare(square);

    // highlight the possible squares for this piece
    for (let i = 0; i < moves.length; i++) {
        greySquare(moves[i].to);
    }
};

let onMouseoutSquare = function (square, piece) {
    removeGreySquares();
};

let onSnapEnd = function () {
    board.position(game.fen());
};

let cfg = {
    draggable: true,
    position: 'start',
    onDragStart: onDragStart,
    onDrop: onDrop,
    onMouseoutSquare: onMouseoutSquare,
    onMouseoverSquare: onMouseoverSquare,
    onSnapEnd: onSnapEnd
};
board = ChessBoard('board', cfg);
board.position(game.fen());