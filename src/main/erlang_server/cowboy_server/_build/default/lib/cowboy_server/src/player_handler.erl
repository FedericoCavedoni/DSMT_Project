
-module(player_handler).

-export([login/2, start/2, word/2, guess/2, wait_for_messages/1]).

% function performed at the beginning to register the process pid to the username
login(JsonMessage, State = {User, Role, P1, P2, WordToGuess}) ->
  % username of the player taken from the json message
  Username = maps:get(<<"username">>, JsonMessage),
  % check if it's already registered
  % if so, we unregister the name and then make the new registration
  Pid = global:whereis_name(Username),
  case Pid of
    undefined ->
      global:register_name(Username, self());
    _ ->
      global:unregister_name(Username),
      global:register_name(Username, self())
  end,
  io:format("registrazione ok ~p~n", [Username]),
  {Username, Role, P1, P2, WordToGuess}.

% function to setup the game, the player process receives the names of the other players' usernames
% and the role he is playing (Player or Guesser)
start(JsonMessage, State = {Username, R, P1, P2, W}) ->
  Player1 = maps:get(<<"otherPlayer1">>, JsonMessage),
  Player2 = maps:get(<<"otherPlayer2">>, JsonMessage),
  % role: Player, Guesser
  Role = maps:get(<<"role">>, JsonMessage),
  % the players exchange the word that needs to be guessed
  {Response, WordToGuess} = word_distribution(Role, [Player1 | Player2]),
  io:format("Word ~p~n", [WordToGuess]),
  {Response, {Username, Role, Player1, Player2, WordToGuess}}.

% function called when a player send a word
word(JsonMessage, State = {Username, Role, Player1, Player2, WordToGuess}) ->
  % get the word the player wrote from the json message
  Word = maps:get(<<"word">>, JsonMessage),
  io:format("Word ~p~n", [Word]),
  % send the word to the other two players
  send_everyone([Player1|Player2], wordFromOthers, Word),
  {Username, Role, Player1, Player2, WordToGuess}.

% function called when the guesser try to guess the word
guess(JsonMessage, State = {Username, Role, Player1, Player2, WordToGuess}) ->
  % get the word the guesser wrote from the json message
  GuessWord = maps:get(<<"word">>, JsonMessage),
  Others = [Player1|Player2],
  L = binary_to_list(GuessWord),
  % make the comparison of the word the guesser said with the word to guess
  % Res = ok if the guesser guessed correctly, Res = no otherwise
  if L == WordToGuess -> Res = ok;
    true -> Res = no
  end,
  % generate a new word to guess
  NewWordToGuess = generate_word(),
  % send it to the other two players
  send_everyone(Others, guessFromOthers, {GuessWord, NewWordToGuess}),
  JsonResp = jsx:encode([{<<"type">>, guessResult}, {<<"msg">>, Res}]),
  {{text, JsonResp}, {Username, Role, Player1, Player2, NewWordToGuess}}.

wait_for_messages(State={Username,Role,Player1,Player2,WordToGuess}) ->
  receive
    {wordFromOthers, Msg} ->
      % received a word from one player
      io:format("ricevuto ~p~n", [Msg]),
      JsonMessage = jsx:encode([{<<"type">>, word}, {<<"msg">>, Msg}]),
      {{text, JsonMessage}, State};

    {guessFromOthers, {Msg, NewWordToGuess}} ->
      % received a word from the guesser
      io:format("~p ricevuto guess ~p~n", [Username, Msg]),
      M = binary_to_list(Msg),
      % make the comparison of the word the guesser said with the word to guess
      % Res = ok if the guesser guessed correctly, Res = no otherwise
      if M == WordToGuess -> Res = ok;
        true -> Res = no
      end,
      % answer to the client with the word the guesser said, the result of the guess attempt and the new word to guess
      JsonRes = jsx:encode([{<<"type">>, guessWord}, {<<"msg">>, Msg},{<<"res">>, Res},{<<"newWord">>, NewWordToGuess}]),
      {{text, JsonRes}, {Username, Role, Player1, Player2, NewWordToGuess}}

  end.


send_everyone([Player1 | Player2], Label, Msg) ->
  % search the pid of the two player processes using their username
  Pid1 = global:whereis_name(Player1),
  Pid2 = global:whereis_name(Player2),
  % send the message to the processes
  Pid1 ! {Label, Msg},
  Pid2 ! {Label, Msg}.


% function used for generating the word and distributing it to the players
word_distribution(Role, [Player | Guesser]) ->
  % if it's the guesser, generates the word to guess
  % if it's the player, wait for the message from the guesser with the word to guess
  if
    Role == <<"Guesser">> ->
      Word = generate_word(),
      send_everyone([Player | Guesser], wordToGuess, Word),
      % the guesser client do not must see the word
      M = "---------",
      JsonMessage = jsx:encode([{<<"type">>, wordToGuess}, {<<"msg">>, M}]);
    true ->
      Word =
        receive
          {wordToGuess, WordToGuess} ->
            io:format("La parola da indovinare: ~p~n", [WordToGuess]),
            WordToGuess;
          _ ->
            io:format("Messaggio non riconosciuto~n"),
            "error"
        end,
      JsonMessage = jsx:encode([{<<"type">>, wordToGuess}, {<<"msg">>, Word}])
    end,
  io:format("Res~p~n", [JsonMessage]),
  {{text, JsonMessage}, Word}.

% generate a random word in the list
generate_word() ->
  Words = ["casa", "tempo", "anno", "giorno", "uomo", "donna", "amore", "vita", "famiglia", "amico",
    "lavoro", "scuola", "panino", "strada", "auto", "musica", "film", "libro", "arte", "natura",
    "mare", "montagna", "sole", "luna", "stella", "colori", "chitarra", "tristezza", "sorriso", "lacrima",
    "risate", "pensiero", "parola", "silenzio", "gioia", "dolore", "sogno", "realtà", "viaggio", "festa",
    "cibo", "acqua", "fuoco", "aria", "terra", "mente", "cuore", "mano", "occhio", "orecchio",
    "bocca", "naso", "piede", "maniera", "modo", "scelta", "ragione", "emozione", "passione", "desiderio",
    "bisogno", "dovere", "diritto", "cambiamento", "esperienza", "ricordo", "speranza", "paura", "coraggio", "sognatore",
    "guerra", "pace", "verità", "bugia", "ammissione", "rifiuto", "entusiasmo", "nostalgia", "mente", "corpo",
    "spirito", "amarezza", "dolcezza", "equilibrio", "insuccesso", "successo", "caso", "destino", "ammirazione", "disprezzo"],
  random:seed(erlang:now()),
  Index = random:uniform(length(Words)),
  lists:nth(Index, Words).
