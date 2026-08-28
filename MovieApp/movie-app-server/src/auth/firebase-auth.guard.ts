import {
  CanActivate,
  ExecutionContext,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import * as admin from 'firebase-admin';

function initFirebase() {
  if (admin.apps.length > 0) return;

  const serviceAccountEnv = process.env.FIREBASE_SERVICE_ACCOUNT;
  if (serviceAccountEnv) {
    try {
      const serviceAccount = JSON.parse(serviceAccountEnv);
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
      });
      return;
    } catch (e) {
      console.warn('Failed to parse FIREBASE_SERVICE_ACCOUNT, falling back to applicationDefault', e);
    }
  }

  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
  });
}

initFirebase();

@Injectable()
export class FirebaseAuthGuard implements CanActivate {
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const authHeader: string | undefined = request.headers['authorization'];

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new UnauthorizedException('Missing or invalid Authorization header');
    }

    const token = authHeader.slice(7);
    try {
      const decoded = await admin.auth().verifyIdToken(token);
      request.user = decoded;
      return true;
    } catch {
      throw new UnauthorizedException('Invalid or expired Firebase token');
    }
  }
}
