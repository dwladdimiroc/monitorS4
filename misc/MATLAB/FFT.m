
clear all;
close all;

random = normrnd(1000,5000,[1 500]);
%random = [0:0.1:16*pi];
figure(1),plot(sin(random)+0.13*sin(3*random), '-')
axis([0 500 -1 1])
NFFT = 1028; %%% NUMERO MAYOR A N MUESTRAS, ES MEJOR SI ES POTENCIA DE 2
FFTE = fft(random, NFFT);
FFTE2 = abs(fft(random, NFFT));
nVals = (0:NFFT-1)/NFFT;
figure(2),plot(nVals, FFTE2, 'x-');


%%%%%%%%%%%%%%%%ENCONTRAR PEAKS%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[pks,locs] = findpeaks(FFTE2);
figure(3);
plot(pks);



