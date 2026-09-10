// Empaqueta game/js/main.js (módulos ES) en un único script clásico
// game/dist/app.js. Así el WebView de Android puede cargar el juego desde
// file:///android_asset sin módulos, intercepciones ni CORS.
// Uso: node tools/build.mjs [--watch]
import * as esbuild from 'esbuild';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const options = {
  entryPoints: [path.join(root, 'game/js/main.js')],
  bundle: true,
  format: 'iife',
  target: ['es2018'],
  outfile: path.join(root, 'game/dist/app.js'),
  sourcemap: false,
  minify: false,
  legalComments: 'none',
  charset: 'utf8',
};

if (process.argv.includes('--watch')) {
  const ctx = await esbuild.context(options);
  await ctx.watch();
  console.log('Vigilando cambios en game/js → game/dist/app.js');
} else {
  await esbuild.build(options);
  console.log('Generado game/dist/app.js');
}
