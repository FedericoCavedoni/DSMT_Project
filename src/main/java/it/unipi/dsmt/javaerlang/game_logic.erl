%%%-------------------------------------------------------------------
%%% @author Giovanni
%%% @copyright (C) 2023, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 09. dic 2023 14:17
%%%-------------------------------------------------------------------
-module(game_logic).
-author("Giovanni").

%% API
-export([create_connections/0, start_game/0, start_player/0, start_guesser/0, stop_game/1, stop_game/2, say_word/2]).

start_game() ->
  Word = generate_word(),
  io:format("Game started, Word: ~s~n", [Word]),
  %create_connections(),
  % creation of the player's processes
  %P1 = spawn('player1@127.0.0.1', fun() -> player(Word, true) end),
  P1 = global:whereis_name(player1),
  P1 ! {Word},
  P1 ! {true},
  %P2 = spawn('player2@127.0.0.1', fun() -> player(Word, false) end),
  P2 = global:whereis_name(player2),
  P2 ! {Word},
  P2 ! {false},
  %P3 = spawn('player3@127.0.0.1', fun() -> guesser() end),
  P3 = global:whereis_name(guesser),
  % broadcast channel
  Broadcast = spawn(fun() -> broadcast_channel(Word, [P1,P2,P3]) end),
  global:register_name(broadcast, Broadcast),
  % server send to the three players the pid of the broadcast channel process
  P1 ! {Broadcast},
  P2 ! {Broadcast},
  P3 ! {Broadcast}.

create_connections() ->
  Nodes = ['main@127.0.0.1', 'player1@127.0.0.1','player2@127.0.0.1','player3@127.0.0.1'],
  lists:foreach(fun(Node) -> connect_to_others(Node, lists:delete(Node, Nodes)) end, Nodes).

connect_to_others(_, []) -> ok;
connect_to_others(Node1, [Node2 | Others]) ->
  net_kernel:connect_node(Node2),
  connect_to_others(Node1, Others).

broadcast_channel(WordToGuess, Subscribers) ->
  receive
    {turn, Sender, Word} ->
      lists:foreach(fun(P) -> P ! {turn, Sender, Word} end, lists:delete(Sender, Subscribers)),
      broadcast_channel(WordToGuess, Subscribers);
    {guess, Sender, Guess} ->
      io:format("guesser said: ~s~n", [Guess]),
      case WordToGuess of
        % TODO
        % mettere logica punteggio
        Word ->
          io:format("Game Win!~n"),
          Sender ! {result, "Game Win"};
        _ ->
          io:format("Game Lose!~n"),
          Sender ! {result, "Game Lose"}
      end
      %lists:foreach(fun(P) -> P ! {guess, Sender, Word} end, [Server | lists:delete(Sender, Subscribers)]),
      %broadcast_channel(Server, Subscribers)
  end.

start_player() ->
  P = spawn(fun() -> player() end),
  case global:whereis_name(player1) of
    undefined -> % Il nome non è registrato
      global:register_name(player1, P);
    _ ->
      global:register_name(player2, P)
  end.

% versione webapp
player() ->
  Word = receive
           {WordServ} -> WordServ
         end,
  First = receive
            {FirstServ} -> FirstServ
          end,
  io:format("Game started, the word is: ~s~n", [Word]),
  % receive the pid of the other player from the server
  Channel = receive
              {Pid} -> Pid
            end,
  % if it's the first player start with the turn
  case First of
    true ->
      % TODO
      % mandare messaggio a client e abilitare pulsante invio messaggio
      none;
    false ->
      none
  end,
  % go to the next turn
  play_turn(Channel).

% versione terminale
player() ->
  Word = receive
           {WordServ} -> WordServ
         end,
  First = receive
           {FirstServ} -> FirstServ
         end,
  io:format("Game started, the word is: ~s~n", [Word]),
  % receive the pid of the other player from the server
  Channel = receive
    {Pid} -> Pid
  end,
  % if it's the first player start with the turn
  case First of
    true ->
      Input = digit_word(),
      Channel ! {turn, self(), Input};
    false ->
      none
  end,
  % go to the next turn
  play_turn(Channel).

digit_word() ->
  io:format("Insert a word:~n"),
  io:get_line("").

% versione webapp
play_turn(Channel)->
  receive
    {turn, Sender, WordTurn} ->
      io:format("~s~n", [WordTurn]);
    {guess, Sender, Guess} ->
      io:format("Guesser said: ~s~n", [Guess])
  end,
  play_turn(Channel).

say_word(Channel, Input) ->
  % cambiare pid con nomi registrati global
  % probabilmente con self() non funziona più
  Channel ! {turn, self(), Input}.

% versione terminale
play_turn(Channel)->
  receive
    {turn, Sender, WordTurn} ->
      io:format("~s~n", [WordTurn]);
    {guess, Sender, Guess} ->
      io:format("Guesser said: ~s~n", [Guess])
  end,
  Input = digit_word(),
  Channel ! {turn, self(), Input},
  play_turn(Channel).

start_guesser() ->
  P = spawn(fun() -> guesser() end),
  global:register_name(guesser, P).

guesser() ->
  io:format("Press 1 when you want to try to guess:~n"),
  Channel = receive
              {Pid} -> Pid
            end,
  guesser_loop(Channel).

guesser_loop(Channel) ->
  receive
    {turn, Sender, Word} ->
      io:format("~s~n", [Word]),
      guesser_loop(Channel);
    {result, Result} ->
      io:format("~s~n", [Result])
  end.

%versione webapp
stop_game(Channel, Guess) ->
  Channel ! {guess, self(), Guess},
  receive
    {result, Result} ->
      io:format("~s~n", [Result])
  end.


% versione terminale
stop_game(Channel) ->
  io:format("Insert the word:~n"),
  Choice = io:get_line(""),
  case Choice of
    "1\n" ->
      io:format("Insert the word:~n"),
      Guess = io:get_line(""),
      Channel ! {guess, self(), Guess},
      receive
        {result, Result} ->
          io:format("~s~n", [Result])
      end;
    _ ->
      stop_game(Channel)
  end.

% generate a random word in the list
generate_word() ->
  Words = ["prova", "test", "vediamo", "se", "funziona", "qualche", "cosa", "ma", "la", "vedo", "bigia"],
  random:seed(erlang:now()),
  Index = random:uniform(length(Words)),
  lists:nth(Index, Words).