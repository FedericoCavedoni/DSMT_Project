
-module(player_handler).
%-behaviour(cowboy_websocket_handler).

-export([login/2, start/2, word/2, guess/2, wait_for_messages/1]).

login(JsonMessage, State = {User, Role, Pid1, Pid2, WordToGuess}) ->
  Username = maps:get(<<"username">>, JsonMessage),
  Pid = global:whereis_name(Username),
  case Pid of
    undefined ->
      global:register_name(Username, self());
    _ ->
      global:unregister_name(Username),
      global:register_name(Username, self())
  end,
  io:format("registrazione ok ~p~n", [Username]),
  {Username, Role, Pid1, Pid2, WordToGuess}.

start(JsonMessage, State = {Username, R, PidPlayer1, PidPlayer2, W}) ->
  io:format("Funzione START~n"),
  io:format("JSON~p~n", [JsonMessage]),
  % otherPlayer1, otherPlayer2, role
  Player1 = maps:get(<<"otherPlayer1">>, JsonMessage),
  io:format("~p~n", [Player1]),
  %PidPlayer1 = global:whereis_name(Player1),
  %io:format("~p~n", [PidPlayer1]),
  Player2 = maps:get(<<"otherPlayer2">>, JsonMessage),
  io:format("~p~n", [Player2]),
  %PidPlayer2 = global:whereis_name(Player2),
  %io:format("~p~n", [PidPlayer2]),
  % role: Generator (Player), Player, Guesser
  Role = maps:get(<<"role">>, JsonMessage),
  io:format("~p~n", [Role]),
  io:format("qui~n"),
  {Response, WordToGuess} = word_distribution(Role, [Player1 | Player2]),
  io:format("Word ~p~n", [WordToGuess]),
  %if
   % Role =/= <<"Guesser">> ->
   %   case Username < Player1 of
   %     false -> wait_for_messages(Role, [Player1 | Player2], WordToGuess)
   %   end;
    %true ->
    %  wait_for_messages(Role, [Player1 | Player2], WordToGuess)
  %end,
  io:format("sino io ahahah"),
  {Response, {Username, Role, Player1, Player2, WordToGuess}}.

word(JsonMessage, State = {Username, Role, Player1, Player2, WordToGuess}) ->
  io:format("Stato ~p~p~p~p~n", [Role, Player1, Player2, WordToGuess]),
  % role: Generator (Player), Player, Guesser
  Word = maps:get(<<"word">>, JsonMessage),
  io:format("Word ~p~n", [Word]),
  %send_everyone([PidPlayer1|PidPlayer2], wordFromOthers, Word),
  send_everyone([Player1|Player2], wordFromOthers, Word),
  %wait_for_messages(Role, [PidPlayer1|PidPlayer2], WordToGuess),
  {Username, Role, Player1, Player2, WordToGuess}.


guess(JsonMessage, State = {Username, Role, Player1, Player2, WordToGuess}) ->
  % role: Generator (Player), Player, Guesser
  io:format("illo guess~n"),
  GuessWord = maps:get(<<"word">>, JsonMessage),
  Others = [Player1|Player2],
  io:format("inviati~n"),
  L = binary_to_list(GuessWord),
  io:format("confronto: ~p~p~n", [L,WordToGuess]),
  if L == WordToGuess -> Res = ok;
    true -> Res = no
  end,
  NewWordToGuess = generate_word(),
  send_everyone(Others, guessFromOthers, {GuessWord, NewWordToGuess}),
  JsonResp = jsx:encode([{<<"type">>, guessResult}, {<<"msg">>, Res}]),
  io:format("ok json guesser~n"),
  %cowboy_websocket:send(ok, JsonMessage),
  %NewWordToGuess = word_distribution(Role, Others),
  %NewWordToGuess ="a",
  io:format("parola guessee ok~n"),
  %wait_for_messages(Role, Others, NewWordToGuess),
  {{text, JsonResp}, {Username, Role, Player1, Player2, NewWordToGuess}}.

wait_for_messages(State={Username,Role,Player1,Player2,WordToGuess}) ->
  io:format("comincio attesa"),
  % TODO mettere after?
  receive
    {wordFromOthers, Msg} ->
      io:format("icevuto ~p~n", [Msg]),
      JsonMessage = jsx:encode([{<<"type">>, word}, {<<"msg">>, Msg}]),
      %cowboy_websocket:send(ok, JsonMessage),
      %if Role == <<"guesser">> ->
      %  wait_for_messages(Role, Others, WordToGuess)
      %end,
      io:format("~p ricevuto ~p~n", [Username, Msg]),
      {{text, JsonMessage}, State};

    {guessFromOthers, {Msg, NewWordToGuess}} ->
      io:format("~p ricevuto guess ~p~n", [Username, Msg]),
      JsonGuessWord = jsx:encode([{<<"type">>, guessWord}, {<<"msg">>, Msg}]),
      M = binary_to_list(Msg),
      if M == WordToGuess -> Res = ok;
        true -> Res = no
      end,
      io:format("ok if~n"),
      JsonRes = jsx:encode([{<<"type">>, guessWord}, {<<"msg">>, Msg},{<<"res">>, Res},{<<"newWord">>, NewWordToGuess}]),
      io:format("ok json~n"),
      %cowboy_websocket:send(ok, JsonMessage),
      %NewWordToGuess = word_distribution(Role, [Player1|Player2]),
      %NewWordToGuess = "a",
      io:format("ok new word~n"),
      DoubleRes = {{text, JsonGuessWord},{text, JsonRes}},
      io:format("double res: ~p~n", [DoubleRes]),
      {{text, JsonRes}, {Username, Role, Player1, Player2, NewWordToGuess}}
      %wait_for_messages(Role, Others, NewWordToGuess);

    %_Other ->
    %  wait_for_messages(Role, Others, WordToGuess)
  end.

send_everyone_old([], _, _) -> ok;
send_everyone_old([Name | Others], Label, Msg) ->
  case global:whereis_name(Name) of
    undefined ->
      io:format("Nessun processo registrato con il nome ~p~n", [Name]);
    Pid ->
      io:format("Pid globale per ~p: ~p~n", [Name, Pid]),
      Pid ! {Label, Msg}
  end,
  send_everyone_old(Others, Label, Msg).

send_everyone([Player1 | Player2], Label, Msg) ->
  Pid1 = global:whereis_name(Player1),
  Pid2 = global:whereis_name(Player2),
  Pid1 ! {Label, Msg},
  Pid2 ! {Label, Msg}.


word_distribution(Role, [Player | Guesser]) ->
  % if it's the generator, generates the word to guess
  if
    Role == <<"Guesser">> ->
      Word = generate_word(),
      send_everyone([Player | Guesser], wordToGuess, Word),
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
            "illoFattaccio"
        end,
      JsonMessage = jsx:encode([{<<"type">>, wordToGuess}, {<<"msg">>, Word}])
    end,
  io:format("Res~p~n", [JsonMessage]),
  {{text, JsonMessage}, Word}.

% generate a random word in the list
generate_word() ->
  Words = ["casa", "tempo", "anno", "giorno", "uomo", "donna", "amore", "vita", "famiglia", "amico",
    "lavoro", "scuola", "città", "strada", "auto", "musica", "film", "libro", "arte", "natura",
    "mare", "montagna", "sole", "luna", "stella", "colori", "felicità", "tristezza", "sorriso", "lacrima",
    "risate", "pensiero", "parola", "silenzio", "gioia", "dolore", "sogno", "realtà", "viaggio", "festa",
    "cibo", "acqua", "fuoco", "aria", "terra", "mente", "cuore", "mano", "occhio", "orecchio",
    "bocca", "naso", "piede", "maniera", "modo", "scelta", "ragione", "emozione", "passione", "desiderio",
    "bisogno", "dovere", "diritto", "cambiamento", "esperienza", "ricordo", "speranza", "paura", "coraggio", "sognatore",
    "guerra", "pace", "verità", "bugia", "ammissione", "rifiuto", "entusiasmo", "nostalgia", "mente", "corpo",
    "spirito", "amarezza", "dolcezza", "equilibrio", "insuccesso", "successo", "caso", "destino", "ammirazione", "disprezzo"],
  random:seed(erlang:now()),
  Index = random:uniform(length(Words)),
  lists:nth(Index, Words).
