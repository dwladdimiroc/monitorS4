%random = normrnd(1000,5000,[1 500]);
%random = [0:0.1:16*pi];

minStream = min(stream);
maxStream = max(stream);
sizeStream = max(size(stream));
potencia = round(log2(sizeStream));

% 3600 muestras
for(i=1:N)
    stream
end

figure(1)

subplot(3,1,1),plot(stream, '-')
axis([0 sizeStream minStream maxStream])
%NFFT = 2^potencia; %%% NUMERO MAYOR A N MUESTRAS, ES MEJOR SI ES POTENCIA DE 2
NFFT = 1028;
FFTE = fft(stream, NFFT);
FFTE2 = abs(fft(stream, NFFT));
nVals = (0:NFFT-1)/NFFT;
subplot(3,1,2), plot(nVals, FFTE2, 'x-');


%%%%%%%%%%%%%%%%ENCONTRAR PEAKS%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[pks,locs] = findpeaks(FFTE2);
subplot(3,1,3), plot(pks);