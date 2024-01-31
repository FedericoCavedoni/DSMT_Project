-module(server_handler).
-export([init/2, websocket_handle/2, websocket_info/2]).


init (Req, State) ->
	{cowboy_websocket, Req, {undefined, undefined, undefined, undefined, ""}}.


websocket_handle(Frame = {text, Json}, State = {Username, Role, Player1, Player2, WordToGuess}) ->
	io:format("[chatroom_websocket] websocket_handle => Frame: ~p, State: ~p~n", [Json, State]),
	DecodedMessage = jsx:decode(Json),
	% the field action can have the following values:
	% login, start, word, wait, guess
	% it's used to decode which action has to be performed
	Action = maps:get(<<"action">>, DecodedMessage),
	{Response, UpdatedState} =
		if
			Action == <<"login">> ->
				S = player_handler:login(DecodedMessage, State),
				{Frame, S};
			Action == <<"start">> ->
				player_handler:start(DecodedMessage, State);
			Action == <<"word">> ->
				S = player_handler:word(DecodedMessage, State),
				{Frame, S};
			Action == <<"wait">> ->
				player_handler:wait_for_messages(State);
			Action == <<"guess">> ->
				player_handler:guess(DecodedMessage, State)
		end,
	io:format("~p Response ~p~n", [Username,Response]),
	{reply, [Response], UpdatedState}.


websocket_info({wordFromOthers, WordMsg}, State) ->
	% reception of a word from another player
	JsonMessage = jsx:encode([{<<"type">>, word}, {<<"msg">>, WordMsg}]),
	{[{text, JsonMessage}], State};
websocket_info({guessFromOthers, {GuessMsg, NewWordToGuess}}, State = {Username, Role, Player1, Player2, WordToGuess}) ->
	% reception of a guessing attempt
	M = binary_to_list(GuessMsg),
	if M == WordToGuess -> Res = ok;
		true -> Res = no
	end,
	JsonMessage = jsx:encode([{<<"type">>, guessWord}, {<<"msg">>, GuessMsg},{<<"res">>, Res},{<<"newWord">>, NewWordToGuess}]),
	{[{text, JsonMessage}], {Username, Role, Player1, Player2, NewWordToGuess}};
websocket_info(Info, State) ->
	io:format("chatroom_websocket:websocket_info(Info, State) => Received info ~p~n", [Info]),
	{ok, State}.
