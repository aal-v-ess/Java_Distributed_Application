# Java_Distributed_Application

This game was developed as a school project and it was my first interaction with the Java programming language.

Bearing in mind that in the development of the distributed version, the TCP protocol was used, in order to obtain a correct implementation, the application is divided into two sides: the server side and the client side (user).

As it is possible to infer, the functioning of the distributed version is only possible if, first, the server is connected.
After the server is waiting for new connections, the client initiates communication with a game start request in which it sends the player's name and the difficulty with which he intends to play.
As soon as the server receives this request he will start a game by answering the client with the game variables he should use and the piece he should paint on the board.

On the client side, when suffering interaction or by the player or by the downward movement of the piece on the board, it is necessary to send the information of the positions that intend to be occupied to the server side to keep the record of the information about that player and only after receive a response, then the customer moves forward with new interaction and consequent validation. This process is repeated throughout the game until it ends.

As soon as the end of the game is reached, that is, it is not possible to add more pieces to the board, a game over message, game over, is sent to the server. The server upon receiving this message will save the score for the player in the score file. Then he signals the customer who has already performed the operation and asks him if he wants to join a new game. If the answer is affirmative, the player will be disconnected from the server in order to connect again with another name if he wishes.

After connecting to the server, the player can view the top ten scores, achieved by users, in the Ranking menu in the menu bar.
